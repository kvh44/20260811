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
