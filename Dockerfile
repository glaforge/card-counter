FROM openjdk:17-slim as build
WORKDIR /workspace/app
COPY gradle gradle
COPY gradlew gradlew
COPY gradle.properties gradle.properties
COPY settings.gradle settings.gradle
COPY build.gradle build.gradle
COPY src src
RUN bash /workspace/app/gradlew shadowJar --no-daemon

FROM openjdk:17-slim
WORKDIR /app
COPY --from=build /workspace/app/build/libs/cardcounter-0.1-all.jar /app/
EXPOSE 8080
CMD ["java", "-jar", "/app/cardcounter-0.1-all.jar"]