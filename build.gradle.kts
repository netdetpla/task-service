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
    implementation("io.lettuce:lettuce-core:5.2.1.RELEASE")
    implementation("org.apache.logging.log4j:log4j-api:2.13.0")
    implementation("org.apache.logging.log4j:log4j-core:2.13.0")
    implementation("io.fabric8:kubernetes-client:4.7.0")
    implementation("com.github.docker-java:docker-java:3.1.5")
    implementation("com.squareup.moshi:moshi-kotlin:1.9.2")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}
tasks {
    named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
        mergeServiceFiles()
        manifest {
            attributes(mapOf("Main-Class" to "org.ndp.service.task.MainKt"))
            attributes(mapOf("Multi-Release" to "true"))
        }
    }
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}
