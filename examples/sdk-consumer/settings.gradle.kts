pluginManagement { repositories { gradlePluginPortal(); mavenCentral(); google() } }
dependencyResolutionManagement {
    repositories {
        maven { url = uri("../../build/staging-repository") }
        google()
        mavenCentral()
    }
}
rootProject.name = "duofold-sdk-consumer"
