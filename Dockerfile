FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY gradlew settings.gradle.kts build.gradle.kts gradle.properties package.json ./
COPY gradle gradle
COPY scripts scripts
RUN ./gradlew dependencies --no-daemon -q
COPY src src
RUN ./gradlew shadowJar --no-daemon -x test

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*-all.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
