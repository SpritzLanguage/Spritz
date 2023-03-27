plugins {
    java
    `maven-publish`
    kotlin("jvm") version "1.6.20"
}

group = "spritz"
version = "1.0.0"

repositories.mavenCentral()

dependencies {
    implementation(kotlin("stdlib-jdk8", "1.6.20"))
    implementation(kotlin("reflect", "1.6.20"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
}

publishing.publications.create<MavenPublication>("maven").from(components["java"])