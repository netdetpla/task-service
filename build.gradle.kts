plugins {
    kotlin("jvm") version "1.3.61"
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

group = "org.ndp"
version = "1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("mysql:mysql-connector-java:8.0.18")
    implementation("me.liuwj.ktorm:ktorm-core:2.6")
    implementation("me.liuwj.ktorm:ktorm-jackson:2.6")
    implementation("me.liuwj.ktorm:ktorm-support-mysql:2.6")
    implementation("org.apache.kafka:kafka-clients:2.4.0")
    implementation("org.apache.kafka:kafka-streams:2.4.0")
    implementation("org.apache.logging.log4j:log4j-api:2.13.0")
    implementation("org.apache.logging.log4j:log4j-core:2.13.0")
    implementation("org.slf4j:slf4j-log4j12:1.7.30")
    implementation("io.fabric8:kubernetes-client:4.7.0")
    implementation("com.github.docker-java:docker-java:3.1.5")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}