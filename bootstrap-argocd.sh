#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"

AWS_REGION="${AWS_REGION:-ca-central-1}"
AWS_PROFILE="${AWS_PROFILE:-default}"
EKS_CLUSTER_NAME="${EKS_CLUSTER_NAME:-eks-cluster-20260819}"
ARGOCD_CHART_VERSION="${ARGOCD_CHART_VERSION:-10.2.1}"

for command_name in aws kubectl helm; do
  if ! command -v "${command_name}" >/dev/null 2>&1; then
    echo "Required command not found: ${command_name}" >&2
    exit 1
  fi
done

#AWS_CLI_ARGS=(--region "${AWS_REGION}")
#if [[ -n "${AWS_PROFILE}" ]]; then
#  AWS_CLI_ARGS+=(--profile "${AWS_PROFILE}")
#fi

echo "AWS_PROFILE: ${AWS_PROFILE}"
echo "Connecting kubectl to ${EKS_CLUSTER_NAME} in ${AWS_REGION}..."
aws eks update-kubeconfig --name "${EKS_CLUSTER_NAME}"

echo "Installing Argo CD chart ${ARGOCD_CHART_VERSION}..."
helm repo add argo https://argoproj.github.io/argo-helm --force-update
helm repo update argo
helm upgrade --install argocd argo/argo-cd \
  --version "${ARGOCD_CHART_VERSION}" \
  --namespace argocd \
  --create-namespace \
  --values "${SCRIPT_DIR}/argocd/values.yaml" \
  --wait \
  --timeout 10m

echo "Registering the users API with Argo CD..."
kubectl apply -f "${SCRIPT_DIR}/argocd/application.yaml"

echo "Argo CD is installed. Check reconciliation with:"
echo "  kubectl get applications -n argocd"
echo "  kubectl get pods -n argocd"
echo "Access the UI with:"
echo "  kubectl port-forward service/argocd-server -n argocd 8080:443"

