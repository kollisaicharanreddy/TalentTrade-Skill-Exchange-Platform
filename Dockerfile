# ==========================================
# Stage 1: Build Phase
# ==========================================
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build

# Set the working directory in the builder container
WORKDIR /app

# Copy the pom.xml file to download dependencies first.
# This enables Docker to cache the dependencies layer.
COPY pom.xml .

# Fetch all dependencies offline. If pom.xml doesn't change, 
# Docker will reuse this cached layer, speeding up subsequent builds.
RUN mvn dependency:go-offline -B

# Copy the source code to the container
COPY src ./src

# Build the application package, skipping tests for faster compilation
# (Tests are typically run in the CI/CD pipeline prior to Dockerization)
RUN mvn clean package -DskipTests

# ==========================================
# Stage 2: Runtime Phase
# ==========================================
FROM eclipse-temurin:21-jre-alpine AS runtime

# Set the working directory in the runtime container
WORKDIR /app

# Create a non-root system user and group 'spring' for security.
# Running as root inside containers is a security vulnerability.
RUN addgroup -S spring && adduser -S spring -G spring

# Copy the built JAR from the builder stage
# Rename it to app.jar for a generic reference
COPY --from=build --chown=spring:spring /app/target/*.jar app.jar

# Switch to the non-root user
USER spring

# Expose the application port
EXPOSE 8081

# Run the application with optimized JVM arguments
# -XX:+UseG1GC: Enables G1 Garbage Collector (good for general microservices)
# -XX:MaxRAMPercentage: Dynamically sets max heap size based on container limits
ENTRYPOINT ["java", "-XX:+UseG1GC", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
