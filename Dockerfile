# Build stage: Use Maven to build and test
FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean test  # Run tests during the build phase

# Build the JAR file (skip tests in this step)
RUN mvn package -DskipTests

# Runtime stage: Use a slim OpenJDK image for the final image
FROM openjdk:17-jdk-slim

# Install Maven in the runtime image
RUN apt-get update && apt-get install -y maven

# Set working directory
WORKDIR /app

# Copy the JAR file from the build stage
COPY --from=build /app/target/ILP_cw1-0.0.1-SNAPSHOT.jar /app/ILP_cw1.jar

# Expose the application port
EXPOSE 8080

# Set the entry point to run the Spring Boot application
ENTRYPOINT ["java", "-jar", "/app/ILP_cw1.jar"]
