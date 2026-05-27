# ── Stage 1: NHS Design System assets ────────────────────────────────────────
# Uses the official Node Alpine image (musl-compatible) to install nhsuk-frontend
# and copy the compiled CSS/JS/assets into the expected resource path.
FROM node:20-alpine AS node-build
WORKDIR /app
COPY package.json package-lock.json* ./
RUN npm install --prefer-offline
COPY scripts scripts
RUN node scripts/copy-assets.js

# ── Stage 2: Gradle build ─────────────────────────────────────────────────────
# Compiles Kotlin and produces the fat JAR.  The NHS assets are already present
# so we skip the npmInstall / copyNhsAssets Gradle tasks.
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY gradlew settings.gradle.kts build.gradle.kts gradle.properties ./
COPY gradle gradle
RUN ./gradlew dependencies --no-daemon -q
COPY src src
# Overlay pre-built static assets from the node stage
COPY --from=node-build /app/src/main/resources/static/nhsuk src/main/resources/static/nhsuk
RUN ./gradlew shadowJar --no-daemon -x test -x npmInstall -x copyNhsAssets

# ── Stage 3: Runtime ──────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*-all.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
