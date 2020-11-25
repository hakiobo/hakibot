import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.0"
}
group = "me.huebnerj"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
    maven(url = "https://dl.bintray.com/kordlib/Kord")
    maven(url = "https://jitpack.io")
}
dependencies {
    testImplementation(kotlin("test-testng"))
    implementation("com.gitlab.kordlib.kord:kord-core:0.6.6")
    implementation("org.mongodb:mongodb-driver-sync:4.1.0")
    implementation("org.litote.kmongo:kmongo:4.1.2")
    implementation("org.slf4j:slf4j-simple:1.7.30")
    implementation("com.github.cregus:expression-evaluator:1.0")

//    implementation("org.litote.kmongo:kmongo-async:4.1.2")
//    implementation("org.litote.kmongo:kmongo-coroutine:4.1.2")
//    implementation("org.mongodb:mongodb-driver-reactivestreams:4.1.0")

}
tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "11"
}