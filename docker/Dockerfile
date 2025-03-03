# Download dependencies
FROM maven:3.9.9-eclipse-temurin-22-jammy AS deps

WORKDIR /app

COPY pom.xml .
COPY jira-platform-api-client/pom.xml jira-platform-api-client/pom.xml
COPY jira-software-api-client/pom.xml jira-software-api-client/pom.xml
COPY das-jira-connector/pom.xml das-jira-connector/pom.xml
COPY docker/settings.xml .

RUN --mount=type=secret,id=github_token \
    export GITHUB_TOKEN="$(cat /run/secrets/github_token)" && \
    mvn -B dependency:go-offline dependency:resolve-plugins -DskipTests \
    --settings settings.xml

# Build Stage
FROM maven:3.9.9-eclipse-temurin-22-jammy AS build

WORKDIR /app

COPY --from=deps /root/.m2 /root/.m2

COPY . .

RUN mvn -B clean package -DskipTests

# Runtime Stage
FROM --platform=${BUILDPLATFORM} eclipse-temurin:22-jre-jammy

ENV DAS_SERVER_PORT=50051

# hadolint ignore=DL3008
RUN apt-get update && \
    apt-get install -y --no-install-recommends netcat-openbsd && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY --from=build /app/das-jira-connector/target/das-jira-connector*.jar ./das-jira-connector.jar

COPY --from=build /app/docker/entrypoint.sh /app/entrypoint.sh

COPY --from=build /app/docker/healthcheck.sh /app/healthcheck.sh

RUN chmod +x /app/entrypoint.sh /app/healthcheck.sh && \
    useradd -m raw && \
    chown -R raw:raw /app

USER raw

EXPOSE ${DAS_SERVER_PORT}

LABEL vendor="RAW Labs SA" \
      product="das-jira" \
      image-type="final" \
      org.opencontainers.image.source="https://github.com/raw-labs/das-jira"

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 CMD ["/app/healthcheck.sh"]

ENTRYPOINT ["/app/entrypoint.sh"]
CMD []
