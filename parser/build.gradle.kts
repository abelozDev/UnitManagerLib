plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
}
group = "ru.maplyb.unitmanagerlib"
java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
dependencies {
}
kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
    }
}
