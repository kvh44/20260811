#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
CONFIG_FILE="${SAVE_BUDGET_CONFIG_FILE:-${SCRIPT_DIR}/save-budget.env}"

if [[ -f "${CONFIG_FILE}" ]]; then
  # shellcheck disable=SC1090
  source "${CONFIG_FILE}"
fi

if [[ $# -gt 0 ]]; then
  action="${1}"
fi

action="${action:-close}"
action="${action}"

case "${action}" in
  open)
    K8S_REPLICAS=1
    ECS_DESIRED_COUNT=1
    ;;
  close)
    K8S_REPLICAS=0
    ECS_DESIRED_COUNT=0
    ;;
  *)
    K8S_REPLICAS=0
    ECS_DESIRED_COUNT=0
    ;;
esac

# Scale EKS deployment and ECS service based on action.
# Environment variables override the values in save-budget.env.
K8S_DEPLOYMENT="${K8S_DEPLOYMENT:-20260819}"
K8S_NAMESPACE="${K8S_NAMESPACE:-default}"

ECS_CLUSTER="${ECS_CLUSTER:-cluster20260811}"
ECS_SERVICE="${ECS_SERVICE:-ecs-service-20260818}"
AWS_REGION="${AWS_REGION:-ca-central-1}"
AWS_PROFILE="${AWS_PROFILE-default}"

AWS_CLI_ARGS=(--region "${AWS_REGION}")
if [[ -n "${AWS_PROFILE}" ]]; then
  AWS_CLI_ARGS+=(--profile "${AWS_PROFILE}")
fi

echo "Setting Kubernetes deployment ${K8S_DEPLOYMENT} in namespace ${K8S_NAMESPACE} to ${K8S_REPLICAS} replica(s)..."
kubectl scale deployment/"${K8S_DEPLOYMENT}" --replicas="${K8S_REPLICAS}" -n "${K8S_NAMESPACE}"

echo "Updating ECS service ${ECS_SERVICE} in cluster ${ECS_CLUSTER} to desired count ${ECS_DESIRED_COUNT}..."
aws ecs update-service \
  --cluster "${ECS_CLUSTER}" \
  --service "${ECS_SERVICE}" \
  --desired-count "${ECS_DESIRED_COUNT}" \
  "${AWS_CLI_ARGS[@]}"

echo "Done: action=${action} (replicas=${K8S_REPLICAS}, desired-count=${ECS_DESIRED_COUNT})."
