output "ecr_repository_url" {
  description = "ECR repository URL used by GitHub Actions when it pushes application images."
  value       = aws_ecr_repository.app.repository_url
}

output "ecs_cluster_name" {
  description = "Value for the ECS_CLUSTER GitHub Actions variable."
  value       = aws_ecs_cluster.app.name
}

output "ecs_service_name" {
  description = "Value for the ECS_SERVICE GitHub Actions variable."
  value       = aws_ecs_service.app.name
}

output "application_url" {
  description = "Public HTTP endpoint provided by the Application Load Balancer."
  value       = "http://${aws_lb.app.dns_name}"
}

output "github_actions_deploy_policy_arn" {
  description = "Attach this policy to the IAM user whose access keys are stored as GitHub Actions secrets."
  value       = aws_iam_policy.github_actions_ecs_deploy.arn
}

output "github_actions_variables" {
  description = "Repository variables required by .github/workflows/ecs-deploy.yml."
  value = {
    AWS_REGION             = var.aws_region
    ECR_REPOSITORY         = aws_ecr_repository.app.name
    ECS_CLUSTER            = aws_ecs_cluster.app.name
    ECS_SERVICE            = aws_ecs_service.app.name
    ECS_TASK_DEFINITION    = ".terraform/task-definition.json"
    CONTAINER_NAME         = var.container_name
    SPRING_PROFILES_ACTIVE = var.spring_profile
  }
}
