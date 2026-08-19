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

Notes
-----
- If container build fails due to environment/OOM/thread limits, build locally and use the runtime Dockerfile (copies jar).
- To change behavior or request more documentation, open an issue or ask here.


Other:
Expose Ecs on public internet: https://www.youtube.com/watch?v=3b1--mUhUhI