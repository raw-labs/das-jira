#!/bin/sh
set -e

exec java ${JAVA_OPTS} -jar das-jira-connector.jar "$@"
