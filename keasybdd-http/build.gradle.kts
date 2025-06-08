plugins {
  id("kotlin-library-conventions")
}

dependencies {
  api(project(":keasybdd-common"))
  api(project(":keasybdd-utils"))
  implementation(libs.bundles.log4j2Ecosystem)
  implementation(libs.bundles.junit5Ecosystem)
  implementation(libs.bundles.javacrumbsEcosystem)
  implementation(libs.bundles.dreamheadEcosystem)
  implementation(libs.bundles.jacksonEcosystem)
  implementation(libs.bundles.cucumberEcosystem)
  implementation(libs.guava)
  implementation(libs.gson)
  implementation(libs.bundles.okHttp3Ecosystem)
  implementation(kotlin("test"))
  implementation("org.springframework.boot:spring-boot-starter-test")
}
