# Download dependencies
FROM maven:3.9.9-eclipse-temurin-22-jammy AS deps

WORKDIR /app

COPY pom.xml .
COPY jira-platform-api-client/pom.xml jira-platform-api-client/pom.xml
COPY jira-software-api-client/pom.xml jira-software-api-client/pom.xml
COPY das-jira-connector/pom.xml das-jira-connector/pom.xml
COPY docker/settings.xml .

RUN --mount=type=secret,id=github_token \
    export GITHUB_TOKEN=$(cat /run/secrets/github_token) && \
    mvn -B dependency:go-offline dependency:resolve-plugins -DskipTests \
    --settings settings.xml

# Build Stage
FROM maven:3.9.9-eclipse-temurin-22-jammy AS build

WORKDIR /app

COPY --from=deps /root/.m2 /root/.m2

COPY . .

RUN mvn -B clean package -DskipTests

#Runtime Stage
FROM --platform=${BUILDPLATFORM} debian:bookworm-slim

ARG AMZN_JDK_VERSION="22.0.2.9-1"

ENV amzn_corretto_bin=java-22-amazon-corretto-jdk_${AMZN_JDK_VERSION}_amd64.deb

# Install necessary packages and Amazon Corretto JDK 21
RUN set -eux \
    && apt-get update \
    && apt-get install -y --no-install-recommends \
        curl wget ca-certificates gnupg software-properties-common fontconfig java-common \
    && wget https://corretto.aws/downloads/resources/${AMZN_JDK_VERSION//-/.}/${amzn_corretto_bin} \
    && dpkg --install ${amzn_corretto_bin} \
    && rm -f ${amzn_corretto_bin} \
    && apt-get purge -y --auto-remove -o APT::AutoRemove::RecommendsImportant=false \
        wget gnupg software-properties-common

# Set environment variables
ENV LANG=C.UTF-8 \
    JAVA_HOME=/usr/lib/jvm/java-22-amazon-corretto \
    PATH="/usr/lib/jvm/java-22-amazon-corretto/bin:${PATH}"

# Create a non-root user 'raw' and switch to it
RUN useradd -m raw
USER raw

# Set the working directory
WORKDIR /app

# Copy the packaged application from the build stage
COPY --from=build /app/das-jira-connector/target/das-jira-connector*.jar ./das-jira-connector.jar

# Expose the application port
EXPOSE 50051

# Set labels as per the SBT configuration
LABEL vendor="RAW Labs SA" \
      product="das-jira" \
      image-type="final" \
      org.opencontainers.image.source="https://github.com/raw-labs/das-jira"

# Set the entry point to run the application
CMD ["java", "-jar", "das-jira-connector.jar"]
