/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Java application project to get you started.
 * For more details on building Java & JVM projects, please refer to https://docs.gradle.org/8.4/userguide/building_java_projects.html in the Gradle documentation.
 */

plugins {
    // Apply the application plugin to add support for building a CLI application in Java.
    application

    id("org.springframework.boot") version "3.0.2"
    id("io.spring.dependency-management") version "1.0.15.RELEASE"
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    // This dependency is used by the application.
    implementation("com.google.guava:guava:32.1.1-jre")

    // Spring boot
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    implementation("org.springframework.boot:spring-boot-starter-security")

    // Argument parser
    implementation("net.sourceforge.argparse4j:argparse4j:0.9.0")

    // Project
    implementation(project(":shared"))
    implementation(project(":cliente")) // Esto es para poder crear el bot desde el servidor
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

application {
    // Define the main class for the application.
    mainClass.set("servidor.App")
}

defaultTasks("build")