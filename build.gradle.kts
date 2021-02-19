import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm") version "1.4.0"
}
application.mainClassName = "BotKt"
group = "me.huebnerj"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
    maven(url = "https://dl.bintray.com/kordlib/Kord")
}
dependencies {
    testImplementation(kotlin("test-testng"))
    implementation("com.gitlab.kordlib.kord:kord-core:0.6.6")
    implementation("org.mongodb:mongodb-driver-sync:4.1.0")
    implementation("org.litote.kmongo:kmongo:4.1.2")
    implementation("org.slf4j:slf4j-simple:1.7.30")

//    implementation("org.litote.kmongo:kmongo-async:4.1.2")
//    implementation("org.litote.kmongo:kmongo-coroutine:4.1.2")
//    implementation("org.mongodb:mongodb-driver-reactivestreams:4.1.0")

}

tasks.jar {
    manifest {
            attributes["Main-Class"] = "BotKt"
        }
    from(sourceSets.main.get().output)
    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
    archiveFileName.set("Hakibot.jar")
}
//val compileJarKotlin: KotlinCompile by tasks
//compileJarKotlin.kotlinOptions.includeRuntime = true

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
    }
}