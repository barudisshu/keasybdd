plugins {
  id("kotlin-library-conventions")
}

dependencies {
  api(project(":keasybdd-common"))
  implementation(libs.bundles.log4j2Ecosystem)
  implementation(libs.bundles.junit5Ecosystem)
  implementation(libs.findbugs)
  implementation(libs.bundles.jacksonEcosystem)
  testImplementation(kotlin("test"))
}
