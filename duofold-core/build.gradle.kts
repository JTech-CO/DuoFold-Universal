plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
    `maven-publish`
}
kotlin {
    @OptIn(org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation::class)
    abiValidation {}
    jvmToolchain(21)

    android {
        compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) }
        namespace = "dev.duofold.core"
        compileSdk = 37
        minSdk = 23
        withHostTest {}
    }
    jvm("desktop") { compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) } }
    sourceSets {
        commonMain.dependencies { api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2") }
        commonTest.dependencies { implementation(kotlin("test")) }
    }
}
publishing {
    repositories { maven { name = "staging"; url = rootProject.layout.buildDirectory.dir("staging-repository").get().asFile.toURI() } }
    publications.withType<MavenPublication>().configureEach {
        pom { name.set("DuoFold Core"); description.set("Platform-independent fold geometry and input contracts. Development preview; redistribution terms pending.") }
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
