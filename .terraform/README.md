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

## EKS, Helm, and Argo CD

The EKS Terraform files create the infrastructure consumed by
`../.github/workflows/eks-deploy.yml` and
`../.github/workflows/eks-helm-deploy.yml`:

- a dedicated VPC, private EKS node subnets, one NAT gateway, and EKS control
  plane logging;
- an EKS cluster, managed node group, standard EKS add-ons, and the EKS OIDC
  provider for future IAM roles for service accounts;
- the `test` ECR repository used by the EKS workflows;
- a GitHub OIDC provider and a branch-restricted deployment role with ECR
  push, EKS discovery, and Kubernetes edit access limited to `default`;
- the Argo CD Helm release and an Argo CD Application that continuously
  reconciles `../helm/20260903` from the `main` branch.

Use credentials that can create VPC, IAM, EKS, ECR, CloudWatch, and Helm
resources. The Helm provider invokes `aws eks get-token`, so the AWS CLI must
also be installed and configured before applying this configuration.

```bash
cd .terraform
terraform init
terraform apply
terraform output github_actions_eks_variables
```

Set the resulting values in GitHub **Settings → Secrets and variables →
Actions → Variables**. In particular, `AWS_ROLE_TO_ASSUME`, `AWS_REGION`,
`ECR_REPOSITORY`, and `EKS_CLUSTER_NAME` are required by both EKS workflows.
Do not run `../bootstrap-argocd.sh` after Terraform has created Argo CD: this
Terraform configuration already owns the same Argo CD Application.

If this AWS account already contains `token.actions.githubusercontent.com`,
the `test` repository, or an EKS cluster with the selected names, import the
existing resource into the Terraform state before applying. Terraform must own
the pre-existing cluster and its VPC resources to manage them safely.

The EKS network intentionally uses one NAT gateway to reduce development cost;
it is not highly available. EKS control plane, EC2 nodes, NAT gateway, and
public IPv4 resources incur AWS charges until you destroy them.

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
