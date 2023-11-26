/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Java application project to get you started.
 * For more details on building Java & JVM projects, please refer to https://docs.gradle.org/8.4/userguide/building_java_projects.html in the Gradle documentation.
 */

plugins {
    // Apply the application plugin to add support for building a CLI application in Java.
    application

    id("com.vaadin") version "24.2.3"
    id("org.springframework.boot") version "3.0.2"
    id("io.spring.dependency-management") version "1.0.15.RELEASE"
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
    maven(url = "https://maven.vaadin.com/vaadin-prereleases")
    maven(url = "https://maven.vaadin.com/vaadin-addons")
}

dependencies {
    // This dependency is used by the application.
    implementation("com.google.guava:guava:32.1.1-jre")

    // Vaadin
    implementation(enforcedPlatform("com.vaadin:vaadin-bom:24.2.3"))
    implementation("com.vaadin:vaadin-core")

    // Spring boot
    implementation("com.vaadin:vaadin-spring-boot-starter")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    implementation("org.springframework.boot:spring-boot-starter-security")

    // Argument parser
    implementation("net.sourceforge.argparse4j:argparse4j:0.9.0")

    // Project
    implementation(project(":shared"))
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

application {
    // Define the main class for the application.
    mainClass.set("cliente.App")
}

defaultTasks("build")

vaadin {

}