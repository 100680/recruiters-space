# Use OpenJDK 21 JDK Slim as base image
FROM openjdk:21-jdk-slim

# Set working directory
WORKDIR /app

# Copy the application's jar to the container
COPY build/libs/ebuy-product-catalog-read-0.0.1-SNAPSHOT.jar app.jar

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]