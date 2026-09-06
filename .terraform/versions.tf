terraform {
  required_version = ">= 1.6.0"

  # tf-deploy.yml supplies the bucket, key, and locking configuration.
  backend "s3" {}

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = ">= 5.0, < 7.0"
    }

    helm = {
      source  = "hashicorp/helm"
      version = ">= 2.15, < 3.0"
    }

    tls = {
      source  = "hashicorp/tls"
      version = ">= 4.0, < 5.0"
    }
  }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = local.common_tags
  }
}
