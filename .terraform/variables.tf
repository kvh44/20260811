variable "aws_region" {
  description = "AWS region in which the ECS resources are created."
  type        = string
  default     = "ca-central-1"
}

variable "project_name" {
  description = "Name used to prefix ECS, IAM, networking, and logging resources."
  type        = string
  default     = "20260901"

  validation {
    condition     = can(regex("^[a-zA-Z0-9-]+$", var.project_name))
    error_message = "project_name may contain only letters, numbers, and hyphens."
  }
}

variable "ecr_repository_name" {
  description = "Name of the ECR repository that GitHub Actions pushes images to."
  type        = string
  default     = "20260901"
}

variable "container_name" {
  description = "Container name in the ECS task definition. It must match CONTAINER_NAME in GitHub Actions."
  type        = string
  default     = "20260901"
}

variable "container_port" {
  description = "Spring Boot container port exposed by the ECS task."
  type        = number
  default     = 8001
}

variable "spring_profile" {
  description = "Default Spring profile in the bootstrap task definition."
  type        = string
  default     = "local"
}

variable "task_cpu" {
  description = "Fargate CPU units for the task."
  type        = number
  default     = 512
}

variable "task_memory" {
  description = "Fargate memory in MiB for the task."
  type        = number
  default     = 1024
}

variable "vpc_cidr" {
  description = "CIDR block for the dedicated public ECS VPC."
  type        = string
  default     = "10.30.0.0/16"
}

variable "log_retention_in_days" {
  description = "Number of days CloudWatch application logs are retained."
  type        = number
  default     = 14
}

variable "health_check_grace_period_seconds" {
  description = "Time ECS gives the Spring Boot container to start before ALB health checks can replace it."
  type        = number
  default     = 300
}

variable "eks_cluster_name" {
  description = "Name of the EKS cluster used by the EKS GitHub Actions workflows."
  type        = string
  default     = "eks-cluster-20260903"
}

variable "eks_kubernetes_version" {
  description = "Optional EKS Kubernetes version. Set null to let EKS select its default supported version."
  type        = string
  default     = null
  nullable    = true
}

variable "eks_vpc_cidr" {
  description = "CIDR block for the EKS VPC. It must not overlap with vpc_cidr."
  type        = string
  default     = "10.40.0.0/16"
}

variable "eks_public_access_cidrs" {
  description = "CIDRs allowed to reach the public EKS API endpoint. GitHub-hosted runners require 0.0.0.0/0 unless you use a self-hosted runner."
  type        = list(string)
  default     = ["0.0.0.0/0"]
}

variable "eks_node_instance_types" {
  description = "EC2 instance types for the EKS managed node group."
  type        = list(string)
  default     = ["t3.medium"]
}

variable "eks_node_min_size" {
  description = "Minimum number of EKS managed nodes."
  type        = number
  default     = 1
}

variable "eks_node_desired_size" {
  description = "Initial number of EKS managed nodes."
  type        = number
  default     = 2
}

variable "eks_node_max_size" {
  description = "Maximum number of EKS managed nodes."
  type        = number
  default     = 3
}

variable "eks_ecr_repository_name" {
  description = "ECR repository pushed by the EKS workflows and referenced by the Helm chart."
  type        = string
  default     = "20260903"
}

variable "github_repository" {
  description = "GitHub owner/repository allowed to assume the EKS deployment role."
  type        = string
  default     = "kvh44/20260811"

  validation {
    condition     = can(regex("^[^/]+/[^/]+$", var.github_repository))
    error_message = "github_repository must use the owner/repository format."
  }
}

variable "github_repository_owner_id" {
  description = "Immutable GitHub owner ID included in this repository's OIDC subject claim."
  type        = string
  default     = "16013107"
}

variable "github_repository_id" {
  description = "Immutable GitHub repository ID included in this repository's OIDC subject claim."
  type        = string
  default     = "1331272653"
}

variable "github_branch" {
  description = "Git branch allowed to assume the EKS deployment role."
  type        = string
  default     = "main"
}

variable "github_actions_eks_role_name" {
  description = "IAM role name published as AWS_ROLE_TO_ASSUME for GitHub Actions."
  type        = string
  default     = "GitHubActionsEKSDeploy-20260903"
}

variable "argocd_namespace" {
  description = "Kubernetes namespace in which Argo CD is installed."
  type        = string
  default     = "argocd"
}

variable "argocd_chart_version" {
  description = "Version of the Argo CD Helm chart to install."
  type        = string
  default     = "10.2.1"
}

variable "argocd_application_name" {
  description = "Name of the Argo CD Application that reconciles the users API Helm chart."
  type        = string
  default     = "users-api"
}

variable "argocd_application_path" {
  description = "Repository-relative Helm chart path reconciled by Argo CD."
  type        = string
  default     = "helm/20260903"
}

variable "argocd_application_namespace" {
  description = "Kubernetes namespace into which Argo CD deploys the users API."
  type        = string
  default     = "default"
}

variable "argocd_application_deployment_name" {
  description = "Deployment name for which Argo CD ignores replica changes made by save-budget.sh."
  type        = string
  default     = "20260903"
}
