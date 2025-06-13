# Use the Gradle image to build the project
FROM gradle:8.11.1-jdk17 AS build

# Set the working directory
WORKDIR /app

# Copy Gradle wrapper and build files first to cache dependencies
COPY build.gradle build.gradle
COPY settings.gradle settings.gradle
COPY gradle gradle

# Download dependencies and cache them to avoid re-downloading
RUN gradle build --no-daemon --dry-run || true

# Run the Spring Boot application from the mountpoint defined in docker-compose.yml
CMD ["gradle", "bootRun", "--no-daemon", "-Dspring.main.sources=com.springboot.Application"]
