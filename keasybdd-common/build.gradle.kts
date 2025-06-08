plugins {
  id("kotlin-library-conventions")
}

dependencies {
  if (JavaVersion.current().isJava9Compatible) {
    compileOnly(libs.javaxAnnotationApi)
  }
  implementation(libs.bundles.log4j2Ecosystem)
  implementation(libs.bundles.arrowEcosystem)
  implementation(libs.guava)
  testImplementation(kotlin("test"))
}
