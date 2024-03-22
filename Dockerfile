# 1. Start with Temurin 21 JDK base image
FROM eclipse-temurin:21-jdk as build

# 2. Set working directory inside the container
WORKDIR /app

# 3. Copy Gradle configuration files
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# 4. Copy your application code
COPY src src
COPY build/resources /app/resources

# 5. Build your application
RUN ./gradlew build -x test

# Use the Temurin 21 JRE for running the application to reduce the image size
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy the built JAR file from the build stage
COPY --from=build /app/build/libs/*.jar csvetlapp.jar


# 6. Run your Spring Boot application
ENTRYPOINT ["java","-jar","csvetlapp.jar"]
