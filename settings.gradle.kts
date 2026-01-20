pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        mavenCentral()
        // If you use other repos, add them here (e.g., google(), mavenLocal())
    }
}

rootProject.name = "KSL-GUI"

include(":app")
include(":utils")
