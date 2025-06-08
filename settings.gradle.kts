// The settings file is the entry point of every Gradle build.
// Its primary purpose is to define the subprojects.
// It is also used for some aspects of project-wide configuration, like managing plugins, dependencies, etc.
// https://docs.gradle.org/current/userguide/settings_file_basics.html

dependencyResolutionManagement {
  @Suppress("UnstableApiUsage")
  includeBuild("build-logic")
  repositories {
    mavenCentral()
  }
}

plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

include(":keasybdd-utils")
include(":keasybdd-steps")
include(":keasybdd-common")
include(":keasybdd-tcp")
include(":keasybdd-http")
include(":keasybdd-sample:keasybdd-spring-app")
include(":keasybdd-sample:keasybdd-spring-ut")
include(":keasybdd-sample:keasybdd-spring-ct")

rootProject.name = "keasybdd"
