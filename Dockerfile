# Stage 1: build with Gradle
FROM gradle:8.7-jdk17 AS build
WORKDIR /home/gradle/project
COPY --chown=gradle:gradle . /home/gradle/project
RUN gradle clean build -x test --no-daemon

# Stage 2: runtime
FROM eclipse-temurin:17-jre
EXPOSE 8080
COPY --from=build /home/gradle/project/build/libs/*.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]