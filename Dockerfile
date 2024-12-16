# Dockerfile
FROM openjdk:21-slim AS gradle-builder
WORKDIR /app

# Copy ONLY what is needed for the gradle build.
COPY gradlew gradlew
COPY gradle/wrapper gradle/wrapper
COPY build.gradle build.gradle
COPY settings.gradle settings.gradle
COPY src src

RUN chmod +x ./gradlew
RUN ./gradlew shadowJar --no-daemon

FROM openjdk:21-slim
WORKDIR /app
COPY --from=gradle-builder /app/build/libs/*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]