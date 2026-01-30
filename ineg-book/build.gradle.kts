plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    // Use whatever KSL dependency/version your capstone already uses:
    implementation("io.github.rossetti:KSLCore:<YOUR_KSL_VERSION>")
}

// Pull sources directly from the INEG project folder (no copy).
sourceSets {
    val main by getting {
        kotlin.srcDir(file("../ineg36204RepositoryF25-AhmedKhan-main/src/main/kotlin"))
        resources.srcDir(file("../ineg36204RepositoryF25-AhmedKhan-main/src/main/resources"))
    }
}
