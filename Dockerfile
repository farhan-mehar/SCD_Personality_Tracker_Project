# ════════════════════════════════════════════════════════════════
# STAGE 1 — Build with Maven + JDK 21
# ════════════════════════════════════════════════════════════════
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Download dependencies first (layer caching — only re-downloads if pom.xml changes)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build JAR (tests run separately in CI)
COPY src ./src
RUN mvn clean package -DskipTests -B

# ════════════════════════════════════════════════════════════════
# STAGE 2 — Minimal runtime with JRE 21 (Alpine = small image)
# ════════════════════════════════════════════════════════════════
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Non-root user for security
RUN addgroup -S psyche && adduser -S psyche -G psyche

# Copy built JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Ownership + switch to non-root
RUN chown -R psyche:psyche /app
USER psyche

# App port
EXPOSE 8080

# Health check — hits the login page (publicly accessible, no auth needed)
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD wget -qO- http://localhost:8080/login || exit 1

# JVM tuning: container-aware memory limits
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-jar", "/app/app.jar"]
