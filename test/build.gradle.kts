plugins {
    kotlin("jvm") version "2.2.20"
}


tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}