data "aws_availability_zones" "available" {
  state = "available"
}

locals {
  resource_prefix = "app-${var.project_name}"
  cluster_name    = "${local.resource_prefix}-cluster"
  service_name    = "${local.resource_prefix}-service"
  task_family     = "${local.resource_prefix}-task"

  availability_zones = slice(data.aws_availability_zones.available.names, 0, 2)

  common_tags = {
    Project   = var.project_name
    ManagedBy = "Terraform"
  }
}

resource "aws_ecr_repository" "app" {
  name                 = var.ecr_repository_name
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = true
  }

  encryption_configuration {
    encryption_type = "AES256"
  }
}

resource "aws_ecr_lifecycle_policy" "app" {
  repository = aws_ecr_repository.app.name

  policy = jsonencode({
    rules = [
      {
        rulePriority = 1
        description  = "Keep the 20 most recent images."
        selection = {
          tagStatus   = "any"
          countType   = "imageCountMoreThan"
          countNumber = 20
        }
        action = {
          type = "expire"
        }
      }
    ]
  })
}

resource "aws_vpc" "ecs" {
  cidr_block           = var.vpc_cidr
  enable_dns_hostnames = true
  enable_dns_support   = true
}

resource "aws_internet_gateway" "ecs" {
  vpc_id = aws_vpc.ecs.id
}

resource "aws_subnet" "public" {
  count = length(local.availability_zones)

  vpc_id                  = aws_vpc.ecs.id
  cidr_block              = cidrsubnet(var.vpc_cidr, 4, count.index)
  availability_zone       = local.availability_zones[count.index]
  map_public_ip_on_launch = true

  tags = {
    Name = "${local.resource_prefix}-public-${count.index + 1}"
  }
}

resource "aws_route_table" "public" {
  vpc_id = aws_vpc.ecs.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.ecs.id
  }
}

resource "aws_route_table_association" "public" {
  count = length(aws_subnet.public)

  subnet_id      = aws_subnet.public[count.index].id
  route_table_id = aws_route_table.public.id
}

resource "aws_security_group" "alb" {
  name        = "${local.resource_prefix}-alb"
  description = "Allow HTTP traffic to the ECS application load balancer."
  vpc_id      = aws_vpc.ecs.id

  ingress {
    description = "HTTP"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_security_group" "task" {
  name        = "${local.resource_prefix}-task"
  description = "Allow the load balancer to reach the Spring Boot container."
  vpc_id      = aws_vpc.ecs.id

  ingress {
    description     = "Spring Boot traffic from the ALB"
    from_port       = var.container_port
    to_port         = var.container_port
    protocol        = "tcp"
    security_groups = [aws_security_group.alb.id]
  }

  ingress {
    description = "Spring Boot traffic from the Internet (IPv4)"
    from_port   = var.container_port
    to_port     = var.container_port
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description      = "Spring Boot traffic from the Internet (IPv6)"
    from_port        = var.container_port
    to_port          = var.container_port
    protocol         = "tcp"
    ipv6_cidr_blocks = ["::/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_cloudwatch_log_group" "app" {
  name              = var.project_name
  retention_in_days = var.log_retention_in_days
}

data "aws_iam_policy_document" "ecs_tasks_assume_role" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["ecs-tasks.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "task_execution" {
  name               = "${local.resource_prefix}-ecs-task-execution"
  assume_role_policy = data.aws_iam_policy_document.ecs_tasks_assume_role.json
}

resource "aws_iam_role_policy_attachment" "task_execution" {
  role       = aws_iam_role.task_execution.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

resource "aws_iam_role" "task" {
  name               = "${local.resource_prefix}-ecs-task"
  assume_role_policy = data.aws_iam_policy_document.ecs_tasks_assume_role.json
}

resource "aws_ecs_cluster" "app" {
  name = local.cluster_name

  setting {
    name  = "containerInsights"
    value = "enabled"
  }
}

resource "aws_lb" "app" {
  name               = "${local.resource_prefix}-alb"
  internal           = false
  load_balancer_type = "application"
  security_groups    = [aws_security_group.alb.id]
  subnets            = aws_subnet.public[*].id
}

resource "aws_lb_target_group" "app" {
  name        = "${local.resource_prefix}-tg"
  port        = var.container_port
  protocol    = "HTTP"
  target_type = "ip"
  vpc_id      = aws_vpc.ecs.id

  health_check {
    enabled             = true
    path                = "/actuator/health"
    matcher             = "200"
    healthy_threshold   = 2
    unhealthy_threshold = 3
    timeout             = 5
    interval            = 30
  }
}

resource "aws_lb_listener" "http" {
  load_balancer_arn = aws_lb.app.arn
  port              = 80
  protocol          = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.app.arn
  }
}

resource "aws_ecs_task_definition" "bootstrap" {
  family                   = local.task_family
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu                      = var.task_cpu
  memory                   = var.task_memory
  execution_role_arn       = aws_iam_role.task_execution.arn
  task_role_arn            = aws_iam_role.task.arn

  # GitHub Actions replaces IMAGE_URI with the immutable ECR image tag before deploying.
  # Terraform only registers this task definition to create the ECS service at desired count zero.
  container_definitions = replace(
    jsonencode(jsondecode(file("${path.module}/task-definition.json")).containerDefinitions),
    "IMAGE_URI",
    "${aws_ecr_repository.app.repository_url}:bootstrap"
  )

  runtime_platform {
    cpu_architecture        = "X86_64"
    operating_system_family = "LINUX"
  }

  depends_on = [
    aws_iam_role_policy_attachment.task_execution,
    aws_cloudwatch_log_group.app
  ]
}

resource "aws_ecs_service" "app" {
  name            = local.service_name
  cluster         = aws_ecs_cluster.app.id
  task_definition = aws_ecs_task_definition.bootstrap.arn
  desired_count   = 0
  launch_type     = "FARGATE"

  enable_execute_command = true

  # Keep the current task serving traffic until its replacement passes the
  # target group's health check. With a maximum of 200%, ECS can run both
  # task revisions during the rolling deployment.
  deployment_minimum_healthy_percent = 100
  deployment_maximum_percent         = 200
  health_check_grace_period_seconds  = var.health_check_grace_period_seconds

  deployment_circuit_breaker {
    enable   = true
    rollback = true
  }

  network_configuration {
    assign_public_ip = true
    security_groups  = [aws_security_group.task.id]
    subnets          = aws_subnet.public[*].id
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.app.arn
    container_name   = var.container_name
    container_port   = var.container_port
  }

  # GitHub Actions owns the deployed image revision and scales the service to one task.
  lifecycle {
    ignore_changes = [desired_count, task_definition]
  }

  depends_on = [aws_lb_listener.http]
}

data "aws_iam_policy_document" "github_actions_ecs_deploy" {
  statement {
    sid       = "EcrAuthorization"
    actions   = ["ecr:GetAuthorizationToken"]
    resources = ["*"]
  }

  statement {
    sid = "PushApplicationImages"
    actions = [
      "ecr:BatchCheckLayerAvailability",
      "ecr:CompleteLayerUpload",
      "ecr:InitiateLayerUpload",
      "ecr:PutImage",
      "ecr:UploadLayerPart"
    ]
    resources = [aws_ecr_repository.app.arn]
  }

  statement {
    sid = "RegisterAndDescribeTaskDefinitions"
    actions = [
      "ecs:DescribeTaskDefinition",
      "ecs:RegisterTaskDefinition"
    ]
    resources = ["*"]
  }

  statement {
    sid = "DeployEcsService"
    actions = [
      "ecs:DescribeServices",
      "ecs:UpdateService"
    ]
    resources = [aws_ecs_service.app.id]
  }

  statement {
    sid       = "DescribeEcsResources"
    actions   = ["ecs:DescribeClusters"]
    resources = ["*"]
  }

  statement {
    sid       = "PassTaskRoles"
    actions   = ["iam:PassRole"]
    resources = [aws_iam_role.task.arn, aws_iam_role.task_execution.arn]

    condition {
      test     = "StringEquals"
      variable = "iam:PassedToService"
      values   = ["ecs-tasks.amazonaws.com"]
    }
  }
}

resource "aws_iam_policy" "github_actions_ecs_deploy" {
  name        = "${local.resource_prefix}-github-actions-ecs-deploy"
  description = "Least-privilege permissions for the GitHub ECS deployment workflow."
  policy      = data.aws_iam_policy_document.github_actions_ecs_deploy.json
}
