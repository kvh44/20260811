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

1. Deploy the IAM role and EKS access entry:
   aws cloudformation deploy \
     --profile default \
     --region ca-central-1 \
     --stack-name github-actions-oidc-20260811 \
     --template-file .aws/github-actions-oidc-role.yml \
     --capabilities CAPABILITY_NAMED_IAM
2. In GitHub, open Settings > Secrets and variables > Actions > Variables and set:
   AWS_ROLE_TO_ASSUME=arn:aws:iam::878915883825:role/GitHubActionsEKSDeploy-20260811
   AWS_REGION=ca-central-1
   ECR_REPOSITORY=test
   EKS_CLUSTER_NAME=eks-cluster-20260819

The role trust policy accepts OIDC tokens only from the main branch of this repository. Its AWS permissions are limited to pushing images to the test ECR repository and describing the EKS cluster. Kubernetes access is limited to edit operations in the default namespace.

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
--name eks-cluster-20260819

# List pods
kubectl get pods --all-namespaces

# Inspect a pod
kubectl describe pod -n default 20260819-7cd5fb6c48-hp6l6

# Read logs
kubectl logs -n default 20260819-7cd5fb6c48-hp6l6 --all-containers

# Open a shell when the pod is Running
kubectl exec -it -n default 20260819-7cd5fb6c48-hp6l6 -- /bin/sh

# Read logs for a specific pod
kubectl logs 20260819-bcbf7d5f7-87trf

# Access the application locally
kubectl port-forward -n default pod/20260819-7cd5fb6c48-hp6l6 8001:8001
kubectl port-forward -n default service/20260819-svc  8001:8001

# Delete pods to force recreation (e.g., after changing the image):
kubectl delete pods -n default -l app=20260819
# Scale down to 0 replicas to stop the deployment:
kubectl scale deployment/20260819 --replicas=0 -n default
# Scale up to 2 replicas to start the deployment:
kubectl scale deployment/20260819 --replicas=2 -n default



AWS EKS deployment with Helm and Argo CD
-----------------------------------------
The Kubernetes Deployment and Service are packaged in `helm/20260819`. Argo CD
tracks that chart on the `main` branch and continuously reconciles it into the
EKS `default` namespace.

The EKS workflow uses GitHub OIDC, so it does not require long-lived AWS access
keys. CI builds and pushes the image, commits the new tag to the Helm values,
and lets Argo CD perform the deployment. CI no longer runs `kubectl apply`.

1. Deploy the IAM role and EKS access entry:
   aws cloudformation deploy \
   --profile default \
   --region ca-central-1 \
   --stack-name github-actions-oidc-20260811 \
   --template-file .aws/github-actions-oidc-role.yml \
   --capabilities CAPABILITY_NAMED_IAM
2. In GitHub, open Settings > Secrets and variables > Actions > Variables and set:
   AWS_ROLE_TO_ASSUME=arn:aws:iam::878915883825:role/GitHubActionsEKSDeploy-20260811
   AWS_REGION=ca-central-1
   ECR_REPOSITORY=test
   EKS_CLUSTER_NAME=eks-cluster-20260819

3. Install Helm locally, then bootstrap Argo CD after this branch is merged to
   `main`:
   brew install helm
   ./bootstrap-argocd.sh

4. Check synchronization:
   kubectl get applications -n argocd
   kubectl get pods -n argocd
   kubectl get deployment,service -n default

Access the Argo CD UI locally:
kubectl port-forward service/argargocdocd-server -n argocd 8080:443

Then open https://localhost:8080. Retrieve the initial admin password with:
kubectl get secret argocd-initial-admin-secret -n argocd \
-o jsonpath='{.data.password}' | base64 --decode; echo

Validate the application chart before committing changes:
helm lint helm/20260819
helm template users-api helm/20260819

The role trust policy accepts OIDC tokens only from the main branch of this
repository. Argo CD owns the live application resources; changes to the chart
or its values are the desired state.

The Argo CD Application ignores only `/spec/replicas` on deployment `20260819`.
This allows `save-budget.sh` to scale the application to zero overnight without
Argo CD immediately restoring it. Argo CD's own pods remain online and continue
to consume a small amount of EKS worker capacity.

Notes
-----
- If container build fails due to environment/OOM/thread limits, build locally and use the runtime Dockerfile (copies jar).
- To change behavior or request more documentation, open an issue or ask here.


Use Mysql in docker
-------------------------
Start mysql in docker:
docker compose up -d
docker compose ps

Log:
docker compose logs -f mysql

Test connection mysal:
docker compose exec mysql mysql -u appuser -papppassword

Create DB:
docker compose exec -T mysql mysql -u appuser -papppassword < init.mysql.sql

Stop docker:
docker compose down

Stop docker and delete data:
docker compose down -v

Others:
-------------------------
Expose Ecs on public internet: https://www.youtube.com/watch?v=3b1--mUhUhI
