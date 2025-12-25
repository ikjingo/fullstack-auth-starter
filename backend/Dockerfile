# Build stage
FROM gradle:8.11-jdk21 AS build
WORKDIR /app
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle
COPY api ./api
COPY core ./core
COPY storage ./storage
RUN gradle dependencies --no-daemon || true
RUN gradle :api:app-api:bootJar --no-daemon

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/api/app-api/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
