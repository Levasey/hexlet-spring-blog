plugins {
    java
    jacoco
    id("org.sonarqube") version "6.3.1.5724"
    id("org.springframework.boot") version "3.5.6"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "io"
version = "0.0.1-SNAPSHOT"
description = "hexlet-spring-blog"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

jacoco {
    toolVersion = "0.8.14"
}


sonar {
    properties {
        property("sonar.projectKey", "Levasey_hexlet-spring-blog")
        property("sonar.organization", "levasey")
        property("sonar.projectName", "Hexlet Spring Blog")
        property("sonar.host.url", "https://sonarcloud.io")
        // Использовать JVM Gradle (например JDK 21 в CI), без запроса /analysis/jres (часто 403 без токена в этом запросе)
        property("sonar.scanner.skipJreProvisioning", "true")

        // Java configuration
        property("sonar.java.source", "21")
        property("sonar.java.target", "21")
        property("sonar.java.binaries", "build/classes")
        property("sonar.java.libraries", "build/libs/*.jar")

        // Coverage configuration
        property("sonar.coverage.jacoco.xmlReportPaths", "build/reports/jacoco/test/jacocoTestReport.xml")
        property("sonar.coverage.exclusions", "**/model/**,**/config/**,**/Application.java")

        // Test configuration
        property("sonar.junit.reportPaths", "build/test-results/test")
        property("sonar.tests", "src/test/java")

        // Source configuration
        property("sonar.sources", "src/main/java")
        property("sonar.tests", "src/test/java")
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("com.bucket4j:bucket4j-core:8.10.1")
    implementation("net.logstash.logback:logstash-logback-encoder:8.1")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.8")
    implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity6")
    testImplementation("org.springframework.security:spring-security-test")


    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    implementation("org.mapstruct:mapstruct:1.6.3")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.6.3")
    runtimeOnly("org.postgresql:postgresql")
    testAnnotationProcessor("org.mapstruct:mapstruct-processor:1.6.3")
    implementation("org.openapitools:jackson-databind-nullable:0.2.6")

    testImplementation("org.instancio:instancio-junit:3.3.0")
    implementation("net.datafaker:datafaker:2.4.2")
    testImplementation("net.javacrumbs.json-unit:json-unit-assertj:4.1.1")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        // Exclude older versions if needed
        exclude(group = "org.mockito", module = "mockito-core")
    }

    // Explicitly include Mockito
    testImplementation("org.mockito:mockito-core:5.14.2")
    testImplementation("org.mockito:mockito-junit-jupiter:5.14.2")

    runtimeOnly("com.h2database:h2")

    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")

    developmentOnly("org.springframework.boot:spring-boot-devtools")
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.7".toBigDecimal()
            }
        }
    }
}

tasks.check {
    dependsOn(tasks.jacocoTestReport, tasks.jacocoTestCoverageVerification)
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
        showExceptions = true
        showCauses = true
        showStackTraces = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }

    systemProperty("spring.profiles.active", "test")
    systemProperty("java.awt.headless", "true")

    // Убедитесь, что тесты выполняются в правильном порядке
    failFast = false
    maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)
}

// Добавьте задачу для очистки перед тестированием
tasks.clean {
    doLast {
        delete("build")
    }
}
