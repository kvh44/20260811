locals {
  eks_resource_prefix = "eks-${var.project_name}"
}

resource "aws_vpc" "eks" {
  cidr_block           = var.eks_vpc_cidr
  enable_dns_hostnames = true
  enable_dns_support   = true

  tags = merge(local.common_tags, {
    Name = "${local.eks_resource_prefix}-vpc"
  })
}

resource "aws_internet_gateway" "eks" {
  vpc_id = aws_vpc.eks.id

  tags = merge(local.common_tags, {
    Name = "${local.eks_resource_prefix}-igw"
  })
}

resource "aws_subnet" "eks_public" {
  count = length(local.availability_zones)

  vpc_id                  = aws_vpc.eks.id
  cidr_block              = cidrsubnet(var.eks_vpc_cidr, 4, count.index)
  availability_zone       = local.availability_zones[count.index]
  map_public_ip_on_launch = true

  tags = merge(local.common_tags, {
    Name                                            = "${local.eks_resource_prefix}-public-${count.index + 1}"
    "kubernetes.io/cluster/${var.eks_cluster_name}" = "shared"
    "kubernetes.io/role/elb"                        = "1"
  })
}

resource "aws_subnet" "eks_private" {
  count = length(local.availability_zones)

  vpc_id            = aws_vpc.eks.id
  cidr_block        = cidrsubnet(var.eks_vpc_cidr, 4, count.index + 8)
  availability_zone = local.availability_zones[count.index]

  tags = merge(local.common_tags, {
    Name                                            = "${local.eks_resource_prefix}-private-${count.index + 1}"
    "kubernetes.io/cluster/${var.eks_cluster_name}" = "shared"
    "kubernetes.io/role/internal-elb"               = "1"
  })
}

resource "aws_route_table" "eks_public" {
  vpc_id = aws_vpc.eks.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.eks.id
  }

  tags = merge(local.common_tags, {
    Name = "${local.eks_resource_prefix}-public"
  })
}

resource "aws_route_table_association" "eks_public" {
  # The subnet resource values are unknown during planning. Its instance
  # count is the same deterministic availability-zone count used above.
  count = length(local.availability_zones)

  subnet_id      = aws_subnet.eks_public[count.index].id
  route_table_id = aws_route_table.eks_public.id
}

resource "aws_eip" "eks_nat" {
  domain = "vpc"

  depends_on = [aws_internet_gateway.eks]

  tags = merge(local.common_tags, {
    Name = "${local.eks_resource_prefix}-nat-eip"
  })
}

# One NAT gateway keeps this development cluster's cost lower. For production,
# use one NAT gateway per availability zone and dedicated private route tables.
resource "aws_nat_gateway" "eks" {
  allocation_id = aws_eip.eks_nat.id
  subnet_id     = aws_subnet.eks_public[0].id

  depends_on = [aws_internet_gateway.eks]

  tags = merge(local.common_tags, {
    Name = "${local.eks_resource_prefix}-nat"
  })
}

resource "aws_route_table" "eks_private" {
  vpc_id = aws_vpc.eks.id

  route {
    cidr_block     = "0.0.0.0/0"
    nat_gateway_id = aws_nat_gateway.eks.id
  }

  tags = merge(local.common_tags, {
    Name = "${local.eks_resource_prefix}-private"
  })
}

resource "aws_route_table_association" "eks_private" {
  # Keep the count plan-time known; see aws_subnet.eks_private above.
  count = length(local.availability_zones)

  subnet_id      = aws_subnet.eks_private[count.index].id
  route_table_id = aws_route_table.eks_private.id
}

resource "aws_cloudwatch_log_group" "eks_cluster" {
  name              = "/aws/eks/${var.eks_cluster_name}/cluster"
  retention_in_days = var.log_retention_in_days
}

resource "aws_eks_cluster" "this" {
  name     = var.eks_cluster_name
  role_arn = aws_iam_role.eks_cluster.arn
  version  = var.eks_kubernetes_version

  enabled_cluster_log_types = ["api", "audit", "authenticator"]

  access_config {
    authentication_mode                         = "API_AND_CONFIG_MAP"
    bootstrap_cluster_creator_admin_permissions = true
  }

  vpc_config {
    endpoint_private_access = true
    endpoint_public_access  = true
    public_access_cidrs     = var.eks_public_access_cidrs
    subnet_ids              = aws_subnet.eks_private[*].id
  }

  depends_on = [
    aws_cloudwatch_log_group.eks_cluster,
    aws_iam_role_policy_attachment.eks_cluster,
  ]
}

resource "aws_eks_node_group" "default" {
  cluster_name    = aws_eks_cluster.this.name
  node_group_name = "${local.eks_resource_prefix}-default"
  node_role_arn   = aws_iam_role.eks_node.arn
  subnet_ids      = aws_subnet.eks_private[*].id

  capacity_type  = "ON_DEMAND"
  instance_types = var.eks_node_instance_types
  disk_size      = 20

  scaling_config {
    min_size     = var.eks_node_min_size
    desired_size = var.eks_node_desired_size
    max_size     = var.eks_node_max_size
  }

  update_config {
    max_unavailable = 1
  }

  depends_on = [
    aws_iam_role_policy_attachment.eks_node,
  ]

  tags = local.common_tags
}

resource "aws_eks_addon" "vpc_cni" {
  cluster_name                = aws_eks_cluster.this.name
  addon_name                  = "vpc-cni"
  resolve_conflicts_on_create = "OVERWRITE"
  resolve_conflicts_on_update = "OVERWRITE"

  depends_on = [aws_eks_node_group.default]
}

resource "aws_eks_addon" "coredns" {
  cluster_name                = aws_eks_cluster.this.name
  addon_name                  = "coredns"
  resolve_conflicts_on_create = "OVERWRITE"
  resolve_conflicts_on_update = "OVERWRITE"

  depends_on = [aws_eks_node_group.default]
}

resource "aws_eks_addon" "kube_proxy" {
  cluster_name                = aws_eks_cluster.this.name
  addon_name                  = "kube-proxy"
  resolve_conflicts_on_create = "OVERWRITE"
  resolve_conflicts_on_update = "OVERWRITE"

  depends_on = [aws_eks_node_group.default]
}

data "tls_certificate" "eks_oidc" {
  url = aws_eks_cluster.this.identity[0].oidc[0].issuer
}

# This provider enables IAM roles for Kubernetes service accounts (IRSA) when
# workloads need AWS permissions in the future.
resource "aws_iam_openid_connect_provider" "eks" {
  url             = aws_eks_cluster.this.identity[0].oidc[0].issuer
  client_id_list  = ["sts.amazonaws.com"]
  thumbprint_list = [data.tls_certificate.eks_oidc.certificates[0].sha1_fingerprint]
}
