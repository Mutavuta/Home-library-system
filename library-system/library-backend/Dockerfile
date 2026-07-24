# Use a multi-stage build for a smaller final image

# Stage 1: Build the application
FROM maven:3.9.6-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean install -DskipTests

# Stage 2: Create the final runnable image
FROM openjdk:17-jdk-slim-bullseye
WORKDIR /app
# Copy the built JAR from the build stage
COPY --from=build /app/target/library-backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]