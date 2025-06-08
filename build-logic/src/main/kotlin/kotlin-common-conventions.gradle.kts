import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

repositories {
  mavenCentral()
}

plugins {
  java
  idea
  signing
  jacoco
  `maven-publish`
  kotlin("jvm")
  kotlin("plugin.lombok")
  id("io.freefair.lombok")
  id("org.jetbrains.dokka")
  id("org.jlleitschuh.gradle.ktlint")
  id("org.springframework.boot")
  id("io.spring.dependency-management")
}

group = "com.cplier"
version = "1.0.0-SNAPSHOT"

java {
  sourceCompatibility = JavaVersion.VERSION_21
  targetCompatibility = JavaVersion.VERSION_21
  withSourcesJar() // Include sources JAR
}

dependencies {
  // Use the Kotlin JDK standard library
  api(kotlin("stdlib", embeddedKotlinVersion))
  api(kotlin("reflect"))
  compileOnly("org.projectlombok:lombok:1.18.38")
  annotationProcessor("org.projectlombok:lombok:1.18.38")
  testImplementation(kotlin("test"))
}

configurations.all {
  exclude(
    group = "org.springframework.boot",
    module = "spring-boot-starter-logging",
  )
  exclude(group = "ch.qos.logback", module = "logback-classic")
}

kotlin {
  jvmToolchain(21)
  sourceSets.all {
    languageSettings {
      languageVersion = "2.1"
    }
  }
  compilerOptions {
    jvmTarget = JvmTarget.JVM_21
    languageVersion.set(KotlinVersion.KOTLIN_2_1)
    apiVersion.set(KotlinVersion.KOTLIN_2_1)
    progressiveMode = true
    freeCompilerArgs.addAll(
      "-Xjvm-default=all",
      "-Xjsr305=strict",
      "-Xexplicit-api=strict",
      "-opt-in=kotlin.contracts.ExperimentalContracts",
      "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
    )
  }
}

tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
  enabled = false
}

tasks.withType<Test>().configureEach {
  useJUnitPlatform()
  testLogging {
    events(TestLogEvent.FAILED, TestLogEvent.PASSED, TestLogEvent.SKIPPED)
  }
  finalizedBy(tasks.jacocoTestReport)
  jvmArgs(
    "-ea",
    "-Djava.awt.headless=true",
    "-Djdk.attach.allowAttachSelf=true",
    "-Dio.netty.tryReflectionSetAccessible=true",
    "-XX:+EnableDynamicAgentLoading",
    "-Xshare:off",
    "--add-opens",
    "java.base/java.lang=ALL-UNNAMED",
    "--add-opens",
    "java.base/java.net=ALL-UNNAMED",
    "--add-opens",
    "java.base/java.lang.reflect=ALL-UNNAMED",
    "--add-opens",
    "java.base/java.time=ALL-UNNAMED",
    "--add-opens",
    "java.base/java.lang.module=ALL-UNNAMED",
    "--add-opens",
    "java.base/java.util=ALL-UNNAMED",
    "--add-opens",
    "java.base/java.util.concurrent=ALL-UNNAMED",
    "--add-opens",
    "java.base/java.io=ALL-UNNAMED",
    "--add-opens",
    "java.base/java.nio=ALL-UNNAMED",
    "--add-opens",
    "java.base/jdk.internal.loader=ALL-UNNAMED",
    "--add-opens",
    "java.base/jdk.internal.ref=ALL-UNNAMED",
    "--add-opens",
    "java.base/jdk.internal.reflect=ALL-UNNAMED",
    "--add-opens",
    "java.base/jdk.internal.math=ALL-UNNAMED",
    "--add-opens",
    "java.base/jdk.internal.module=ALL-UNNAMED",
  )
  include("**/ComponentTest*")
}

jacoco {
  toolVersion = "0.8.10"
}

idea {
  module.isDownloadJavadoc = true
  module.isDownloadSources = true
}

ktlint {
  version.set("1.6.0")
  android = true
  ignoreFailures = false
  enableExperimentalRules = true
  filter {
    include("**/kotlin/**")
    exclude("**/generated/**")
    exclude("**/test/**")
  }
}

tasks.jacocoTestReport {
  dependsOn(tasks.test) // Ensure tests run before generating the report
  reports {
    xml.required.set(true) // Enable XML report
    html.required.set(true) // Enable HTML report
    csv.required.set(false) // Disable CSV report
  }
}

tasks.dokkaHtml {
  outputDirectory.set(buildDir.resolve("dokka"))
  dokkaSourceSets {
    configureEach {
      sourceLink {
        localDirectory.set(file("src/main/kotlin"))
        remoteUrl.set(
          uri(
            "https://github.com/barudisshu/keasybdd/keasybdd-core/tree/main/src/main/kotlin",
          ).toURL(),
        )
        remoteLineSuffix.set("#L")
      }
    }
  }
}

val sourcesjar: TaskProvider<Jar> by tasks.registering(Jar::class) {
  from(sourceSets.getByName("main").allSource)
  archiveClassifier.set("sources")
}

tasks.jar {
  manifest {
    attributes(
      "Implementation-Title" to project.name,
      "Implementation-Version" to project.version,
    )
  }
}

tasks.withType<JavaCompile>().configureEach {
  options.encoding = "UTF-8"
}

publishing {
  publications {
    create<MavenPublication>("maven") {
      from(components["kotlin"])
      artifact(sourcesjar.get())
      pom {
        name.set("Kotlin Easy BDD")
        description.set("An BDD framework for spring project")
        url.set("https://github.com/barudisshu/keasybdd")
        licenses {
          license {
            name.set("The Apache License, Version 2.0")
            url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
          }
        }
        developers {
          developer {
            id.set("barudisshu")
            name.set("Barudisshu")
            email.set("galudisu@gmail.com")
            url.set("https://galudisu.info")
            roles.set(listOf("owner", "developer"))
          }
        }
        scm {
          connection.set("scm:git:git@://github.com/barudisshu/keasybdd.git")
          developerConnection.set(
            "scm:git:git@://github.com/barudisshu/keasybdd.git",
          )
          url.set("https://github.com/barudisshu/keasybdd")
          tag.set("HEAD")
        }
        inceptionYear.set("2025")
      }
    }
    repositories {
      mavenCentral()
    }
  }
}
