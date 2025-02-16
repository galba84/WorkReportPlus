# Use an official OpenJDK runtime as a parent image
FROM openjdk:17-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the built JAR file into the container
COPY build/libs/WorkReportPlus-0.0.1-SNAPSHOT.jar app.jar

# Expose port 8080 to the outside
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
