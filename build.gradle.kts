plugins {
    kotlin("jvm")
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.formdev:flatlaf:3.1") // Modern Swing look
    implementation("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.9.0")
    api(group = "io.github.rossetti", name = "KSLCore", version = "R1.2.5")
}

application {
    mainClass.set("gui.MainWindow") // Your main class
}
