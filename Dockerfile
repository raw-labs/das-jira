# Build Stage
FROM maven:3.9.9-amazoncorretto-23-debian-bookworm AS build

WORKDIR /app

# Copy the entire project into the build context
COPY . .

# Build the Maven project
RUN mvn clean package -DskipTests

# Runtime Stage
FROM amazoncorretto:21

# Set labels
LABEL vendor="RAW Labs SA"
LABEL product="das-jira-server"
LABEL image-type="final"
LABEL org.opencontainers.image.source="https://github.com/raw-labs/das-jira"

# Set environment variables
ENV LANG="C.UTF-8"

# Create non-root user 'raw' and switch to it
RUN useradd -ms /bin/bash raw
USER raw

# Set working directory
WORKDIR /home/raw

# Copy the built application JAR from the build stage
COPY --from=build /app/das-jira-connector/target/das-jira-connector-*.jar ./app.jar

# Expose ports and volumes
EXPOSE 50051
VOLUME ["/var/log/raw"]

# Set the entry point to run your application
CMD ["java", "-jar", "app.jar"]
