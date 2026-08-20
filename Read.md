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

CI
--
GitHub Actions workflow at .github/workflows/maven.yml uses Java 26 and runs package via ./mvnw or ./mvnw replacement. Dependency-graph upload is tolerant to disabled repos.

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


Other:
Expose Ecs on public internet: https://www.youtube.com/watch?v=3b1--mUhUhI
