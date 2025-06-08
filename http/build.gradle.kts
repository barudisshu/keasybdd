plugins {
  id("kotlin-library-conventions")
}

dependencies {
  implementation(libs.bundles.kotlinxEcosystem)
  testImplementation(kotlin("test"))
}
