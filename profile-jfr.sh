#!/usr/bin/env bash
set -euo pipefail

readonly project_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
cd "$project_dir"

./mvnw -q -DskipTests package
mkdir -p target/jfr

shopt -s nullglob
application_jars=(target/*.jar)
shopt -u nullglob

if (( ${#application_jars[@]} != 1 )); then
  echo "Expected exactly one application JAR in target/, found ${#application_jars[@]}." >&2
  exit 1
fi

export SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-local}"
readonly jfr_duration="${JFR_DURATION:-2m}"
readonly jfr_max_size="${JFR_MAX_SIZE:-256m}"

exec java \
  "-XX:StartFlightRecording=name=users-api,settings=profile,duration=${jfr_duration},maxsize=${jfr_max_size},dumponexit=true,filename=target/jfr/users-api-%p-%t.jfr" \
  -jar "${application_jars[0]}" "$@"
