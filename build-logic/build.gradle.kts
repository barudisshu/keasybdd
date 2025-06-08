plugins {
  // The Kotlin DSL plugin provides a convenient way to develop convention plugins.
  // Convention plugins are located in `src/main/kotlin`, with the file extension `.gradle.kts`,
  // and are applied in the project's `build.gradle.kts` files as required.
  `kotlin-dsl`
}

repositories {
  mavenCentral()
  gradlePluginPortal()
}

kotlin {
  jvmToolchain(21)
}

dependencies {
  // Add a dependency on the Kotlin Gradle plugin, so that convention plugins can apply it.
  implementation(libs.kotlinGradlePlugin)
  implementation(libs.ktlintGradlePlugin)
  implementation(libs.dokkaGradlePlugin)
  implementation(libs.lombokGradlePlugin)
  implementation(libs.freefairGradlePlugin)
  implementation(libs.depMgtGradlePlugin)
  implementation(libs.springGradlePlugin)
}
