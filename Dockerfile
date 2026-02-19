# ===== Build stage =====
FROM gradle:8.14-jdk21 AS build
WORKDIR /app

COPY build.gradle settings.gradle ./
COPY gradle ./gradle

# Pre-download dependencies (caches Gradle wrapper deps)
RUN gradle dependencies --no-daemon || true

# Copy application source
COPY src ./src

# Build the application (no tests)
RUN gradle build -x test --no-daemon

# ===== Runtime stage =====
FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=dev"]
