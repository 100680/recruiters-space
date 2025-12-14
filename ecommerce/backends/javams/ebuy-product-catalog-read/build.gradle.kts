plugins {
    java
    id("org.springframework.boot") version "3.5.5"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.ebuy"
version = "0.0.1-SNAPSHOT"
description = "eBuy Product Catalog Read Service - Read-optimized APIs for product data"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral() // Make sure this is present
}

dependencies {
    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-actuator:3.5.5")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb:3.5.5")
    implementation("org.springframework.boot:spring-boot-starter-web:3.5.5")
    implementation("org.springframework.boot:spring-boot-starter-validation:3.5.5")


    // Lombok
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")

    // Test dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-test:3.5.5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
