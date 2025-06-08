plugins {
  id("kotlin-application-conventions")
}

dependencies {
  implementation(libs.gson)
  implementation(libs.guava)
  implementation(libs.bundles.okHttp3Ecosystem)
  implementation(libs.bundles.log4j2Ecosystem)
  implementation(libs.bundles.jacksonEcosystem)
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("org.springframework.boot:spring-boot-starter-aop")
  implementation("org.springframework.boot:spring-boot-starter-log4j2")
  testApi(project(":keasybdd-steps"))
  testApi(project(":keasybdd-utils"))
  testApi(project(":keasybdd-steps"))
  testApi(project(":keasybdd-common"))
  testApi(project(":keasybdd-tcp"))
  testApi(project(":keasybdd-http"))
  testApi(project(":keasybdd-sample:keasybdd-spring-app"))
  testImplementation(libs.bundles.junit5Ecosystem)
  testImplementation(kotlin("test"))
  testImplementation("org.springframework.boot:spring-boot-starter-test")
}
