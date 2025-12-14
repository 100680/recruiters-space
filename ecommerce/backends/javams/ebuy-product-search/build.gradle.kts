plugins {
    java
    id("org.springframework.boot") version "3.3.5"  // Fixed version - 3.5.5 doesn't exist
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.ebuy"
version = "0.0.1-SNAPSHOT"
description = "eBuy Product Search and Autocomplete Service"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-actuator") {
        exclude(group = "commons-logging", module = "commons-logging")
    }
    implementation("org.springframework.boot:spring-boot-starter-validation") {
        exclude(group = "commons-logging", module = "commons-logging")
    }
    implementation("org.springframework.boot:spring-boot-starter-web") {
        exclude(group = "commons-logging", module = "commons-logging")
    }

    // OpenSearch Client - Updated compatible versions
    implementation("org.opensearch.client:opensearch-java:2.13.0") {
        exclude(group = "commons-logging", module = "commons-logging")
    }
    implementation("org.opensearch.client:opensearch-rest-client:2.13.0") {
        exclude(group = "commons-logging", module = "commons-logging")
    }

    // Apache HttpComponents - Updated versions for better compatibility
    implementation("org.apache.httpcomponents:httpclient:4.5.14") {
        exclude(group = "commons-logging", module = "commons-logging")
    }
    implementation("org.apache.httpcomponents:httpcore:4.4.16")
    implementation("org.apache.httpcomponents:httpasyncclient:4.1.5") {
        exclude(group = "commons-logging", module = "commons-logging")
    }

    // Jackson for JSON processing
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.core:jackson-core")
    implementation("com.fasterxml.jackson.core:jackson-annotations")

    // Lombok
    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")

    // Lombok for tests
    testCompileOnly("org.projectlombok:lombok:1.18.34")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.34")

    // Devtools
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-parameters")
}

tasks.withType<Test> {
    useJUnitPlatform()
}