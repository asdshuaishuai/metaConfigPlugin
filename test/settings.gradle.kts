
pluginManagement {
    includeBuild("../d2GradleSample")
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
    id("com.d2rabbit.meta-config")
}
rootProject.name = "test"