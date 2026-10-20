
# Stage 1: Build Application
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app

# Copy maven descriptor and source code
COPY pom.xml .
COPY src ./src

# Build the executable jar without running unit tests
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy the built jar from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose Spring Boot default port
EXPOSE 8080

# Run Spring Boot application
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
