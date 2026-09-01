# ECS and ECR Terraform

This configuration creates the AWS resources required by
`../.github/workflows/ecs-deploy.yml`:

- an ECR repository with image scanning and a 20-image retention policy;
- a dedicated VPC with two public subnets, an Internet Gateway, and route
  table associations;
- a public Application Load Balancer that checks `/actuator/health`;
- an ECS Fargate cluster, task definition, and service;
- CloudWatch Logs and the task execution/task IAM roles;
- a least-privilege IAM policy for the GitHub Actions identity that pushes
  ECR images and deploys the ECS service.

The ECS service is intentionally created with zero tasks. The first successful
GitHub workflow run replaces the `IMAGE_URI` placeholder in
`task-definition.json`, registers that task definition, and scales the
service to one task. This avoids Terraform trying to start a task before an
image exists in ECR.

## Deploy the infrastructure

```bash
cd .terraform
cp terraform.tfvars.example terraform.tfvars
terraform init
terraform plan
terraform apply
```

Terraform state and `terraform.tfvars` are ignored by Git. Use an encrypted
remote state backend before sharing this infrastructure with a team.

## Configure GitHub Actions

Attach the policy Terraform creates to the IAM user whose access keys are
stored as the `AWS_ACCESS_KEY_ID` and `AWS_SECRET_ACCESS_KEY` GitHub
secrets:

```bash
aws iam attach-user-policy \
  --user-name <github-actions-iam-user> \
  --policy-arn "$(terraform output -raw github_actions_deploy_policy_arn)"
```

Then set the repository variables printed by Terraform:

```bash
terraform output github_actions_variables
```

Set the following names exactly in GitHub **Settings → Secrets and variables →
Actions → Variables**:

- `AWS_REGION`
- `ECR_REPOSITORY`
- `ECS_CLUSTER`
- `ECS_SERVICE`
- `ECS_TASK_DEFINITION` — keep the value
  `task-definition.json`
- `CONTAINER_NAME`
- `SPRING_PROFILES_ACTIVE`

The task definition uses IAM role names rather than account-specific ARNs, so
it can be committed safely. Its defaults match `terraform.tfvars.example`.
If you change the project name, region, container name, or port, update
`task-definition.json` to match before deploying.

## Cost and cleanup

This configuration creates a public Application Load Balancer and Fargate
tasks. Both incur AWS charges while active. To remove the infrastructure:

```bash
terraform destroy
```
