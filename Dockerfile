# Use Debian Bookworm slim as the base image with amd64 architecture
FROM --platform=amd64 debian:bookworm-slim

# Set labels
LABEL vendor="RAW Labs SA"
LABEL product="das-jira-server"
LABEL image-type="final"
LABEL org.opencontainers.image.source="https://github.com/raw-labs/das-salesforce"

# Expose environment variables
ENV LANG=C.UTF-8
ENV JAVA_HOME=/usr/lib/jvm/java-21-amazon-corretto
ENV PATH=$JAVA_HOME/bin:$PATH

# Expose volumes and ports
VOLUME ["/var/log/raw"]
EXPOSE 50051

# Switch to root user to install JDK and other dependencies
USER root

# Install required dependencies, Amazon Corretto JDK, and cleanup
RUN set -eux \
    && apt-get update \
    && apt-get install -y --no-install-recommends \
        curl wget ca-certificates gnupg software-properties-common fontconfig java-common \
    && wget $amzn_corretto_bin_dl_url/$amzn_corretto_bin \
    && dpkg --install $amzn_corretto_bin \
    && rm -f $amzn_corretto_bin \
    && apt-get purge -y --auto-remove -o APT::AutoRemove::RecommendsImportant=false \
        wget gnupg software-properties-common \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# Switch to non-root user 'raw'
USER raw

# Copy application files (adjust paths as needed)
COPY ./target/universal/stage /opt/das-salesforce-server

# Set working directory
WORKDIR /opt/das-salesforce-server

# Define the entry point for the application
CMD ["bin/das-salesforce-server"]