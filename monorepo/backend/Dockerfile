# Build Stage
FROM gradle:8.5-jdk17 AS builder
WORKDIR /app
COPY . .
# Skip tests for faster build in this example, or remove -x test to run them
# Skip tests for faster build in this example, or remove -x test to run them
RUN gradle spotlessApply
RUN gradle build -x test --no-daemon

# Run Stage
FROM eclipse-temurin:17-jre
# Install curl for healthcheck
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=80.0"
ENTRYPOINT ["java", "-jar", "app.jar"]
