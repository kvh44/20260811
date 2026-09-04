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

output "eks_cluster_name" {
  description = "Value for the EKS_CLUSTER_NAME GitHub Actions variable."
  value       = aws_eks_cluster.this.name
}

output "eks_cluster_endpoint" {
  description = "EKS Kubernetes API endpoint."
  value       = aws_eks_cluster.this.endpoint
}

output "eks_ecr_repository_url" {
  description = "ECR repository URL pushed by the EKS workflows."
  value       = aws_ecr_repository.eks.repository_url
}

output "github_actions_eks_role_arn" {
  description = "Value for the AWS_ROLE_TO_ASSUME GitHub Actions variable."
  value       = aws_iam_role.github_actions_eks.arn
}

output "github_actions_terraform_role_arn" {
  description = "Value for the TERRAFORM_AWS_ROLE_TO_ASSUME GitHub Actions variable."
  value       = aws_iam_role.github_actions_terraform.arn
}

output "github_actions_eks_variables" {
  description = "Repository variables required by .github/workflows/eks-deploy.yml and .github/workflows/eks-helm-deploy.yml."
  value = {
    AWS_REGION         = var.aws_region
    AWS_ROLE_TO_ASSUME = aws_iam_role.github_actions_eks.arn
    EKS_ECR_REPOSITORY = aws_ecr_repository.eks.name
    EKS_CLUSTER_NAME   = aws_eks_cluster.this.name
    ARGOCD_NAMESPACE   = var.argocd_namespace
    ARGOCD_APPLICATION = var.argocd_application_name
  }
}

output "github_actions_terraform_variables" {
  description = "Repository variables required by .github/workflows/tf-deploy.yml."
  value = {
    AWS_REGION                   = var.aws_region
    TERRAFORM_AWS_ROLE_TO_ASSUME = aws_iam_role.github_actions_terraform.arn
  }
}
