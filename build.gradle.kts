plugins {
    kotlin("jvm") version "2.2.0"
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.0"
    id("org.jetbrains.compose") version "1.7.0-alpha03"
}

repositories {
    mavenCentral()
    google()
}

group = "org.example"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(compose.desktop.currentOs)
    testImplementation(kotlin("test"))
    api(group = "io.github.rossetti", name = "KSLCore", version = "R1.2.5")
}

compose.desktop {
    application {
        mainClass = "Main.kt"
    }
}

kotlin {
    jvmToolchain(21)
}

