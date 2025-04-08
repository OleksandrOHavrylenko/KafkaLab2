# Stage 1: Build the application
FROM gradle:7.6.0-jdk17 AS build
WORKDIR /app

# Copy the Gradle wrapper and build files
COPY gradlew .
COPY gradle ./gradle

# Copy the build configuration and source code
COPY build.gradle .
COPY src ./src

# Build the application
RUN ./gradlew shadowJar

# Stage 2: Create the runtime image
FROM openjdk:17-jdk-slim
WORKDIR /app

# Copy the JAR file from the build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Copy the subreddits.csv file from the input directory
COPY input/subreddits.csv /app/input/subreddits.csv

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]