dependencyResolutionManagement {

  // Use Maven Central and the Gradle Plugin Portal for resolving dependencies in the shared build logic (`build-logic`) project.
  @Suppress("UnstableApiUsage")
  repositories {
    mavenCentral()
  }

  // Reuse the version catalog from the main build.
  versionCatalogs {
    create("libs") {
      from(files("../gradle/libs.versions.toml"))
    }
  }
}

rootProject.name = "build-logic"
