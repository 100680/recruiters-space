import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("org.springframework.boot") version "3.5.5"
    id("io.spring.dependency-management") version "1.1.6"
    id("org.asciidoctor.jvm.convert") version "3.3.2"
    id("java")
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    kotlin("plugin.jpa") version "1.9.25"
    kotlin("kapt") version "1.9.25"
}

group = "com.ebuy"
version = "1.0.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

extra["springCloudVersion"] = "2024.0.0"
extra["testcontainersVersion"] = "1.20.2"
extra["mapstructVersion"] = "1.6.3"

dependencies {
    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-webflux") // For reactive clients

    // Spring Cloud
    implementation("org.springframework.cloud:spring-cloud-starter-bootstrap")
    implementation("org.springframework.cloud:spring-cloud-starter-config")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
    implementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j")
    implementation("org.springframework.cloud:spring-cloud-starter-loadbalancer")
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")

    // Database
    implementation("org.postgresql:postgresql")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")

    // Redis for caching
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.session:spring-session-data-redis")

    // Messaging
    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.springframework.cloud:spring-cloud-stream")
    implementation("org.springframework.cloud:spring-cloud-stream-binder-kafka")

    // MapStruct for mapping
    implementation("org.mapstruct:mapstruct:${property("mapstructVersion")}")
    kapt("org.mapstruct:mapstruct-processor:${property("mapstructVersion")}")

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // Jackson for JSON processing
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    // OpenAPI/Swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")

    // Micrometer for metrics
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("io.micrometer:micrometer-tracing-bridge-otel")
    implementation("io.opentelemetry:opentelemetry-exporter-zipkin")

    // Retry and circuit breaker
    implementation("org.springframework.retry:spring-retry")
    implementation("io.github.resilience4j:resilience4j-spring-boot3")
    implementation("io.github.resilience4j:resilience4j-reactor")

    // Logging
    implementation("net.logstash.logback:logstash-logback-encoder:8.0")

    // Utilities
    implementation("org.apache.commons:commons-lang3")
    implementation("org.apache.commons:commons-collections4:4.4")

    // Additional dependencies for high availability
    implementation("org.springframework.retry:spring-retry")
    implementation("org.springframework:spring-aspects")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")

    // Observability
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")

    // Event processing
    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")

    // Async processing
    implementation("org.springframework.boot:spring-boot-starter-web")

    // Development tools
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    // Configuration processor
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    // Test dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:kafka")
    testImplementation("org.testcontainers:redis")
    testImplementation("com.github.tomakehurst:wiremock-jre8:3.0.1")
    testImplementation("org.awaitility:awaitility:4.2.2")
    testImplementation("org.mockito:mockito-inline")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")

    // Architecture tests
    testImplementation("com.tngtech.archunit:archunit-junit5:1.3.0")

    // Performance testing
    testImplementation("org.openjdk.jmh:jmh-core:1.37")
    testImplementation("org.openjdk.jmh:jmh-generator-annprocess:1.37")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
        mavenBom("org.testcontainers:testcontainers-bom:${property("testcontainersVersion")}")
    }
}

// Kapt configuration for MapStruct
kapt {
    arguments {
        arg("mapstruct.defaultComponentModel", "spring")
        arg("mapstruct.unmappedTargetPolicy", "WARN")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()

    // Test configuration for high-availability scenarios
    systemProperty("spring.profiles.active", "test")
    systemProperty("logging.level.com.ebuy", "DEBUG")

    // Parallel test execution for faster builds
    maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)

    // Memory settings for large test suites
    minHeapSize = "512m"
    maxHeapSize = "2g"

    // Testcontainers configuration
    systemProperty("testcontainers.reuse.enable", "true")
    systemProperty("testcontainers.reuse.hashfile", ".testcontainers-hash")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf(
        "-Xlint:unchecked",
        "-Xlint:deprecation",
        "-parameters"
    ))
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict", "-Xjvm-default=all")
        jvmTarget = "21"
    }
}

tasks.withType<BootJar> {
    archiveBaseName.set("ebuy-order-service")
    archiveVersion.set(project.version.toString())

    // Layered JAR for better Docker image caching
    layered {
        application {
            intoLayer("spring-boot-loader") {
                include("org/springframework/boot/loader/**")
            }
            intoLayer("application")
        }
        dependencies {
            intoLayer("dependencies") {
                includeProjectDependencies()
            }
            intoLayer("spring-boot-dependencies") {
                include("org/springframework/boot/**")
            }
            intoLayer("snapshot-dependencies") {
                include("*:*:*SNAPSHOT")
            }
            intoLayer("dependencies")
        }
        layerOrder = listOf(
            "dependencies",
            "spring-boot-dependencies",
            "snapshot-dependencies",
            "spring-boot-loader",
            "application"
        )
    }
}

// Custom task for generating Docker image
tasks.register("dockerImage") {
    group = "docker"
    description = "Builds Docker image for the Order Service"
    dependsOn("bootJar")

    doLast {
        exec {
            commandLine("docker", "build", "-t", "ebuy/order-service:${project.version}", ".")
        }
    }
}

// Custom task for performance testing
tasks.register<Test>("performanceTest") {
    group = "verification"
    description = "Runs performance tests"

    include("**/*PerformanceTest.class")
    shouldRunAfter("test")

    systemProperty("spring.profiles.active", "performance-test")
    maxHeapSize = "4g"
}

// Custom task for integration testing
tasks.register<Test>("integrationTest") {
    group = "verification"
    description = "Runs integration tests"

    include("**/*IntegrationTest.class")
    shouldRunAfter("test")

    systemProperty("spring.profiles.active", "integration-test")
    systemProperty("testcontainers.reuse.enable", "true")
}

// Task for checking code quality
tasks.register("qualityCheck") {
    group = "verification"
    description = "Runs all quality checks"
    dependsOn("test", "integrationTest")
}

// Production readiness checks
tasks.register("productionReadiness") {
    group = "verification"
    description = "Runs all production readiness checks"
    dependsOn("qualityCheck", "performanceTest")
}