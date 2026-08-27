#!/bin/bash
set -e

export JAVA_HOME="$(/usr/libexec/java_home -v 26)"
export PATH="$JAVA_HOME/bin:$PATH"

./mvnw -q -DskipTests package

docker rm -f 20260811-container >/dev/null 2>&1 || true
docker compose -f compose.app.yml -f compose.mysql.yml up --build

echo "Container started. Check with: curl http://localhost:8001/users"
