plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
    id("maven-publish")
}
group = "ru.maplyb.unitmanagerlib"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
publishing {
    publications {
        register<MavenPublication>("release") {
            afterEvaluate {
                from(components["java"])
                groupId = "com.github.abelozDev"
                artifactId = "parser"
                version = libs.versions.lib.version
            }
        }
    }
}
dependencies {
}
kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
    }
}
