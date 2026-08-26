#!/bin/bash
set -e

export JAVA_HOME="$(/usr/libexec/java_home -v 26)"
export PATH="$JAVA_HOME/bin:$PATH"

./mvnw -q -DskipTests package

docker build -t 20260811 .
docker rm -f 20260811-container >/dev/null 2>&1 || true
docker run -e SPRING_PROFILES_ACTIVE=local -d --name 20260811-container -p 8001:8001 20260811

echo "Container started. Check with: curl http://localhost:8001/users"
