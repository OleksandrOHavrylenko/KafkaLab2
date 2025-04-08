# Stage 1: Build the application
FROM gradle:8.13.0-jdk17-alpine AS build
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

ENV OUTPUT_TOPIC="subreddits"
ENV BOOTSTRAP_SERVERS="broker-1:19092, broker-2:19092, broker-3:19092"
ENV CLIENT_ID="subreddits-producer"
ENV ACKS="1"
ENV INPUT_FILE="input/subreddits.csv"

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]