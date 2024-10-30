# Build Stage
FROM maven:3.9.9-amazoncorretto-23-debian-bookworm as build

# Set the working directory inside the container
WORKDIR /app

# Copy the pom files and source code
COPY pom.xml .
COPY jira-platform-api-client/pom.xml jira-platform-api-client/pom.xml
COPY jira-platform-api-client/src jira-platform-api-client/src
COPY jira-software-api-client/pom.xml jira-software-api-client/pom.xml
COPY jira-software-api-client/src jira-software-api-client/src
COPY das-jira-connector/pom.xml das-jira-connector/pom.xml
COPY das-jira-connector/src das-jira-connector/src

# Build the project and package the application
RUN mvn clean package -DskipTests

# Runtime Stage
FROM --platform=amd64 debian:bookworm-slim

# Environment variables for Amazon Corretto JDK 21
ENV amzn_jdk_version=23.0.1.8-1 \
    amzn_corretto_bin=java-23-amazon-corretto-jdk_23.0.1.8-1_amd64.deb \
    amzn_corretto_bin_dl_url=https://corretto.aws/downloads/resources/23.0.1.8.1

# Install necessary packages and Amazon Corretto JDK 21
RUN set -eux \
    && apt-get update \
    && apt-get install -y --no-install-recommends \
        curl wget ca-certificates gnupg software-properties-common fontconfig java-common \
    && wget ${amzn_corretto_bin_dl_url}/${amzn_corretto_bin} \
    && dpkg --install ${amzn_corretto_bin} \
    && rm -f ${amzn_corretto_bin} \
    && apt-get purge -y --auto-remove -o APT::AutoRemove::RecommendsImportant=false \
        wget gnupg software-properties-common

# Set environment variables
ENV LANG=C.UTF-8 \
    JAVA_HOME=/usr/lib/jvm/java-21-amazon-corretto \
    PATH="/usr/lib/jvm/java-23-amazon-corretto/bin:${PATH}"

# Create a non-root user 'raw' and switch to it
RUN useradd -m raw
USER raw

# Set the working directory
WORKDIR /app

# Copy the packaged application from the build stage
COPY --from=build /app/das-jira-connector/target/das-jira-connector.jar ./das-jira-connector.jar

# Expose the application port
EXPOSE 50051

# Set labels as per the SBT configuration
LABEL vendor="RAW Labs SA" \
      product="das-jira" \
      image-type="final" \
      org.opencontainers.image.source="https://github.com/raw-labs/das-jira"

# Set the entry point to run the application
CMD ["java", "-jar", "das-jira-connector.jar"]