plugins {
    kotlin("jvm") version "2.4.20"
    application
}
kotlin {
    jvmToolchain(21)
    compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) }
}
java { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
dependencies { implementation("dev.duofold:duofold-compose:1.0.0-dev.1") }
application { mainClass = "MainKt" }
