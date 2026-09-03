Read.md

Overview
--------
Small Spring Boot app (Java 26) exposing a /users endpoint. Includes a controller, global exception advice for 404, and helper scripts for Docker and hooks.

Run locally
-----------
1. Ensure JDK 26 is installed and JAVA_HOME is set:
   export JAVA_HOME="$(/usr/libexec/java_home -v 26)"
   export PATH="$JAVA_HOME/bin:$PATH"
2. Build with the wrapper:
   ./mvnw -DskipTests package
3. Run:
   java -jar target/20260811-0.0.1-SNAPSHOT.jar

Docker
------
Build & run (runtime-only image expects a built jar):
  ./run-docker.sh
or manually:
  ./mvnw -DskipTests package
  docker build -t 20260811 .
  docker run -d --name 20260811 -p 8001:8001 20260811

API
---
GET /users -> returns JSON list of users
404s are handled by ControllerAdvice returning JSON {"error":"Page not found","status":404,"path":"..."}

Git hooks
---------
Install local commit-msg hook (enforces "#123456: message detail"):
  ./install-hooks.sh


AWS EKS deployment
------------------
The EKS workflow uses GitHub OIDC, so it does not require long-lived AWS access keys.

1. Provision the EKS, ECR, GitHub OIDC role, and EKS access entry with Terraform:
   cd .terraform
   terraform init

   terraform import \
   aws_iam_openid_connect_provider.github_actions \
   arn:aws:iam::878915883825:oidc-provider/token.actions.githubusercontent.com

   terraform plan

   aws iam get-open-id-connect-provider \
   --profile default \
   --open-id-connect-provider-arn \
   arn:aws:iam::878915883825:oidc-provider/token.actions.githubusercontent.com

   terraform apply
   terraform output github_actions_eks_variables
2. In GitHub, open Settings > Secrets and variables > Actions > Variables and set the values from `github_actions_eks_variables`:
   AWS_ROLE_TO_ASSUME=<terraform output>
   AWS_REGION=ca-central-1
   EKS_ECR_REPOSITORY=20260903
   EKS_CLUSTER_NAME=eks-cluster-20260903

The role trust policy accepts OIDC tokens only from the main branch of this repository. Its AWS permissions are limited to pushing images to the EKS ECR repository and describing the EKS cluster. Kubernetes access is limited to edit operations in the default namespace.

Notes
-----
- If container build fails due to environment/OOM/thread limits, build locally and use the runtime Dockerfile (copies jar).
- To change behavior or request more documentation, open an issue or ask here.



Eks:
# View current AWS CLI configuration
aws configure list
aws configure list-profiles

# Refresh kubeconfig if needed
aws eks update-kubeconfig \
--profile default \
--region ca-central-1 \
--name eks-cluster-20260903

# List pods
kubectl get pods --all-namespaces

# Inspect a pod
kubectl describe pod -n default 20260903-7cd5fb6c48-hp6l6

# Read logs
kubectl logs -n default 20260903-7cd5fb6c48-hp6l6 --all-containers

# Open a shell when the pod is Running
kubectl exec -it -n default 20260903-7cd5fb6c48-hp6l6 -- /bin/sh

# Read logs for a specific pod
kubectl logs 20260903-bcbf7d5f7-87trf

# Access the application locally
kubectl port-forward -n default pod/20260903-7cd5fb6c48-hp6l6 8001:8001
kubectl port-forward -n default service/20260903-svc  8001:8001

# Delete pods to force recreation (e.g., after changing the image):
kubectl delete pods -n default -l app=20260903
# Scale down to 0 replicas to stop the deployment:
kubectl scale deployment/20260903 --replicas=0 -n default
# Scale up to 2 replicas to start the deployment:
kubectl scale deployment/20260903 --replicas=2 -n default



AWS EKS deployment with Helm and Argo CD
-----------------------------------------
The Kubernetes Deployment and Service are packaged in `helm/20260903`. Argo CD
tracks that chart on the `main` branch and continuously reconciles it into the
EKS `default` namespace.

The EKS workflow uses GitHub OIDC, so it does not require long-lived AWS access
keys. CI builds and pushes the image, commits the new tag to the Helm values,
and lets Argo CD perform the deployment. CI no longer runs `kubectl apply`.

1. Provision the EKS, ECR, GitHub OIDC role, and EKS access entry with Terraform:
   cd .terraform
   terraform init
   terraform plan
   terraform apply
   terraform output github_actions_eks_variables
2. In GitHub, open Settings > Secrets and variables > Actions > Variables and set the values from `github_actions_eks_variables`:
   AWS_ROLE_TO_ASSUME=<terraform output>
   AWS_REGION=ca-central-1
   EKS_ECR_REPOSITORY=20260903
   EKS_CLUSTER_NAME=eks-cluster-20260903

3. Terraform installs and configures Argo CD; do not run `./bootstrap-argocd.sh`
   afterward because it would manage the same Argo CD Application.

4. Check synchronization:
   kubectl get applications -n argocd
   kubectl get pods -n argocd
   kubectl get deployment,service -n default

Access the Argo CD UI locally:
kubectl port-forward service/argocd-server -n argocd 8080:443

Then open https://localhost:8080. Retrieve the initial admin password with:
kubectl get secret argocd-initial-admin-secret -n argocd \
-o jsonpath='{.data.password}' | base64 --decode; echo

Validate the application chart before committing changes:
helm lint helm/20260903
helm template users-api helm/20260903

The role trust policy accepts OIDC tokens only from the main branch of this
repository. Argo CD owns the live application resources; changes to the chart
or its values are the desired state.

The Argo CD Application ignores only `/spec/replicas` on deployment `20260903`.
This allows `save-budget.sh` to scale the application to zero overnight without
Argo CD immediately restoring it. Argo CD's own pods remain online and continue
to consume a small amount of EKS worker capacity.

Notes
-----
- If container build fails due to environment/OOM/thread limits, build locally and use the runtime Dockerfile (copies jar).
- To change behavior or request more documentation, open an issue or ask here.


Use Mysql in docker
-------------------------
Start app and mysql in docker:
docker compose -f compose.app.yml -f compose.mysql.yml up --build
docker compose -f compose.app.yml -f compose.mysql.yml ps

Or start seperately:
docker compose -f compose.mysql.yml up --build


Log:
docker compose -f compose.mysql.yml logs -f mysql

Test connection mysql:
docker compose -f compose.mysql.yml exec mysql mysql -u appuser -papppassword

Create DB:
docker compose -f compose.mysql.yml exec -T mysql mysql -u appuser -papppassword < init.mysql.sql

Stop and remove docker containers:
docker compose -f compose.mysql.yml -f compose.app.yml down

Stop docker and delete data:
docker compose -f compose.mysql.yml -f compose.app.yml down -v

Others:
-------------------------
Expose Ecs on public internet: https://www.youtube.com/watch?v=3b1--mUhUhI
