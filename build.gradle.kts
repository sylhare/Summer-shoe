import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    val kotlinVersion = "1.7.20"
    id("java")
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
    id("org.springframework.boot") version "2.7.5"
    id("io.spring.dependency-management") version "1.1.0"
    id("jacoco")
    `maven-publish`
    distribution
}

group = "sun.flower"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.14.0")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    //springboot
    implementation("org.springframework.boot:spring-boot-starter") {
        exclude(module = "spring-aop")
    }
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-actuator") // for production metrics

    //memcached
    implementation("net.spy:spymemcached:2.12.3")

    //resilient4j
    implementation("io.github.resilience4j:resilience4j-retry:1.7.1")
    implementation("io.github.resilience4j:resilience4j-circuitbreaker:1.7.1")

    // tests
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(module = "mockito-core")
    }
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.1")
    testImplementation("org.testcontainers:testcontainers:1.17.5")
    testImplementation("org.testcontainers:junit-jupiter:1.17.5")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "${JavaVersion.VERSION_11}"
}
