import com.github.gradle.node.npm.task.NpmTask

plugins {
    kotlin("jvm") version "2.1.0"
    kotlin("plugin.serialization") version "2.1.0"
    id("com.github.node-gradle.node") version "7.0.2"
    id("com.gradleup.shadow") version "8.3.6"
    application
}

group = "com.nhstrial"
version = "1.0-SNAPSHOT"

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

repositories {
    mavenCentral()
}

val ktorVersion = "3.0.3"
val exposedVersion = "0.53.0"

dependencies {
    // Ktor server
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-html-builder:$ktorVersion")
    implementation("io.ktor:ktor-server-sessions:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages:$ktorVersion")
    implementation("io.ktor:ktor-server-default-headers:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // Database
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("org.postgresql:postgresql:42.7.4")
    implementation("org.flywaydb:flyway-core:10.20.1")
    implementation("org.flywaydb:flyway-database-postgresql:10.20.1")

    // Logging
    implementation("ch.qos.logback:logback-classic:1.5.12")

    // Testing
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:2.1.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testImplementation("org.jsoup:jsoup:1.18.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

node {
    version.set("20.17.0")
    download.set(true)
}

val copyNhsAssets = tasks.register<NpmTask>("copyNhsAssets") {
    dependsOn(tasks.npmInstall)
    args.set(listOf("run", "copy-assets"))
    inputs.dir("node_modules/nhsuk-frontend")
    outputs.dir("src/main/resources/static/nhsuk")
}

tasks.processResources {
    dependsOn(copyNhsAssets)
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}
