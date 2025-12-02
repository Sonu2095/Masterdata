# Use a lightweight JDK base image
FROM docker.io/openjdk:17

# Set a working directory
WORKDIR /app

# Copy the Spring Boot JAR file into the image
COPY samsbridge*.jar app.jar

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
