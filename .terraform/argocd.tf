data "aws_eks_cluster" "this" {
  name = aws_eks_cluster.this.name
}

provider "helm" {
  kubernetes {
    host                   = data.aws_eks_cluster.this.endpoint
    cluster_ca_certificate = base64decode(data.aws_eks_cluster.this.certificate_authority[0].data)

    exec {
      api_version = "client.authentication.k8s.io/v1beta1"
      command     = "aws"
      args = [
        "eks",
        "get-token",
        "--cluster-name",
        aws_eks_cluster.this.name,
        "--region",
        var.aws_region,
      ]
    }
  }
}

resource "helm_release" "argocd" {
  name             = "argocd"
  repository       = "https://argoproj.github.io/argo-helm"
  chart            = "argo-cd"
  version          = var.argocd_chart_version
  namespace        = var.argocd_namespace
  create_namespace = true

  atomic          = true
  cleanup_on_fail = true
  wait            = true
  timeout         = 900

  values = [file("${path.module}/../argocd/values.yaml")]

  depends_on = [
    aws_eks_addon.coredns,
    aws_eks_addon.kube_proxy,
    aws_eks_addon.vpc_cni,
  ]
}

# Helm installs this resource only after the Argo CD chart has registered its
# Application CRD, avoiding the two-pass bootstrap required by kubernetes_manifest.
resource "helm_release" "argocd_application" {
  name      = "argocd-application"
  chart     = "${path.module}/charts/argocd-application"
  namespace = var.argocd_namespace

  atomic          = true
  cleanup_on_fail = true
  wait            = true
  timeout         = 300

  values = [templatefile("${path.module}/charts/argocd-application/values.yaml.tftpl", {
    application_name            = var.argocd_application_name
    argocd_namespace            = var.argocd_namespace
    github_repository           = var.github_repository
    github_branch               = var.github_branch
    application_path            = var.argocd_application_path
    application_namespace       = var.argocd_application_namespace
    application_deployment_name = var.argocd_application_deployment_name
  })]

  depends_on = [helm_release.argocd]
}
