plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    `maven-publish`
}
kotlin {
    @OptIn(org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation::class)
    abiValidation {}
    jvmToolchain(21)

    android {
        compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) }
        namespace = "dev.duofold.compose"
        androidResources { enable = true }
        compileSdk = 37
        minSdk = 33
        withHostTest {}
        withDeviceTest { instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner" }
    }
    jvm("desktop") { compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) } }
    sourceSets {
        commonMain.dependencies {
            api(project(":duofold-core"))
            api(compose.ui)
            implementation(compose.runtime)
        }
        androidMain.dependencies { implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2") }
        named("desktopMain").dependencies { implementation(compose.desktop.currentOs) }
        commonTest.dependencies { implementation(kotlin("test")) }
        named("androidDeviceTest").dependencies {
            implementation("androidx.test:runner:1.6.2")
            implementation("androidx.test.ext:junit:1.2.1")
        }
    }
}
publishing {
    repositories { maven { name = "staging"; url = rootProject.layout.buildDirectory.dir("staging-repository").get().asFile.toURI() } }
    publications.withType<MavenPublication>().configureEach {
        pom { name.set("DuoFold Compose"); description.set("Compose fold surface renderer and platform input adapters. Development preview; redistribution terms pending.") }
    }
}






// Supports direct JUnit execution on Windows hosts affected by Unicode Gradle worker paths.
tasks.register("writeDesktopTestClasspath") {
    dependsOn("desktopTestClasses")
    doLast {
        val test = tasks.named<Test>("desktopTest").get()
        layout.buildDirectory.file("desktop-test-classpath.txt").get().asFile.writeText(test.classpath.asPath)
    }
}
