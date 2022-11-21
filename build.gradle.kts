import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm") version "1.4.30"
    java
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

group = "me.huebnerj"
version = "1.0.6"

repositories {
    mavenCentral()
    jcenter()
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}
dependencies {
    testImplementation(kotlin("test-testng"))
    implementation("dev.kord", "kord-core", "0.7.0-RC2")
    implementation("org.slf4j", "slf4j-simple", "1.7.30")
    implementation("org.litote.kmongo", "kmongo-coroutine", "4.2.4")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
    }
}

application {
    mainClassName = "BotKt"
}