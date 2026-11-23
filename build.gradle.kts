import org.gradle.api.tasks.testing.logging.TestExceptionFormat

allprojects {
    apply(plugin = "java-library")

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
    }
}


plugins {
    id("java-library")
    id("org.springframework.boot") version "4.1.2"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "gg.modl.backend"
version = "2.4.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
    maven("https://nexus.modl.gg/repository/maven-releases/")
}

dependencies {
    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")

    // Rate limiting
    implementation("com.bucket4j:bucket4j_jdk17-core:8.22.0")

    // External Services
    implementation("io.github.cdimascio:dotenv-java:3.2.0")

    implementation("com.google.api:api-common:2.73.0")

    implementation("jakarta.annotation:jakarta.annotation-api:3.0.0")
    api("org.jspecify:jspecify:1.0.1")

    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    // AWS S3 (Backblaze B2)
    implementation(platform("software.amazon.awssdk:bom:2.59.5"))
    implementation("software.amazon.awssdk:s3")

    // WebAuthn / Passkeys
    implementation("com.yubico:webauthn-server-core:2.10.1")

    // Replay format
    implementation("gg.modl.minecraft.replay:replay-format:1.1.0")

    // Shared protobuf types
    implementation("gg.modl:proto:1.6.1")
    implementation("com.google.protobuf:protobuf-java:4.35.1")
    implementation("com.google.protobuf:protobuf-java-util:4.35.1")

    // Caching
    implementation("com.github.ben-manes.caffeine:caffeine:3.3.1")

    // Utilities
    implementation("com.github.slugify:slugify:4.0.1")

    // Compile-only / APs
    compileOnly("org.jetbrains:annotations:26.1.0")
    compileOnly("org.projectlombok:lombok")
    compileOnly(project(":mongo-fields-annotations"))
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor(project(":mongo-fields-processor"))

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("com.google.code.gson:gson:2.15.0")
    testImplementation("org.mongodb:mongodb-driver-sync")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}


tasks.withType<JavaCompile> {
    options.compilerArgs.add("-parameters")
    options.compilerArgs.add("-Xlint:deprecation")
}

tasks.test {
    useJUnitPlatform()

    testLogging {
        events("failed", "skipped")
        exceptionFormat = TestExceptionFormat.FULL
        showStandardStreams = false
    }
}
