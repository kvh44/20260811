#!/usr/bin/env bash
set -euo pipefail

# Bootstrap the GitHub Actions OIDC role used by tf-deploy.yml. Run this once
# with local AWS credentials that can manage IAM resources; the GitHub workflow
# cannot assume this role before it exists.
SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
TERRAFORM_DIR="${SCRIPT_DIR}/.terraform"
OIDC_PROVIDER_ADDRESS="aws_iam_openid_connect_provider.github_actions"

for command_name in aws terraform; do
  if ! command -v "${command_name}" >/dev/null 2>&1; then
    echo "Required command not found: ${command_name}" >&2
    exit 1
  fi
done

if [[ ! -d "${TERRAFORM_DIR}" ]]; then
  echo "Terraform directory not found: ${TERRAFORM_DIR}" >&2
  exit 1
fi

AWS_CLI_ARGS=()
if [[ -n "${AWS_PROFILE:-}" ]]; then
  AWS_CLI_ARGS+=(--profile "${AWS_PROFILE}")
fi

cd "${TERRAFORM_DIR}"
terraform init

# GitHub's OIDC provider is account-wide and may already have been created by
# CloudFormation or an earlier Terraform run. Import it into this state before
# applying so Terraform does not try to create a duplicate provider.
terraform_state="$(terraform state list 2>/dev/null || true)"
if [[ "${terraform_state}" != *"${OIDC_PROVIDER_ADDRESS}"* ]]; then
  oidc_provider_arn="$(aws "${AWS_CLI_ARGS[@]}" iam list-open-id-connect-providers \
    --query "OpenIDConnectProviderList[?contains(Arn, 'token.actions.githubusercontent.com')].Arn | [0]" \
    --output text)"

  if [[ -n "${oidc_provider_arn}" && "${oidc_provider_arn}" != "None" ]]; then
    echo "Importing the existing GitHub Actions OIDC provider into Terraform state..."
    terraform import "${OIDC_PROVIDER_ADDRESS}" "${oidc_provider_arn}"
  fi
fi

terraform apply \
  -target=aws_iam_role.github_actions_terraform \
  -target=aws_iam_role_policy_attachment.github_actions_terraform_administrator

echo
echo "Set this GitHub Actions repository variable:"
echo "TERRAFORM_AWS_ROLE_TO_ASSUME=$(terraform output -raw github_actions_terraform_role_arn)"
