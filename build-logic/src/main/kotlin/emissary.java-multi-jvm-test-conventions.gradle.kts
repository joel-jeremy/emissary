plugins {
  id("emissary.java-conventions")
}

val test by testing.suites.existing(JvmTestSuite::class);

additionalTestRunsOnJvmVersions().forEach { additionalJavaVersion ->
  val testTaskName = "testOnJava${additionalJavaVersion}"

  val testTask = tasks.register<Test>(testTaskName) {
    useJUnitPlatform()
    javaLauncher = javaToolchains.launcherFor {
      languageVersion = additionalJavaVersion
    }
    testClassesDirs = files(test.map { it.sources.output.classesDirs })
    classpath = files(test.map { it.sources.runtimeClasspath })
  }

  tasks.named("check") {
    dependsOn(testTask)
  }
}

/**
 * Ideally every LTS release (succeeding the version used in source compilation) 
 * plus the latest released non-LTS version.
 */
fun additionalTestRunsOnJvmVersions(): List<JavaLanguageVersion> {
  // 25 is enabled by default (Default java-conventions toolchain is 25)
  val defaultJvmVersions = "11,17,21"
  val jvmVersions = findProperty("additionalTestRunsOnJvmVersions") as String?
      ?: defaultJvmVersions
  return jvmVersions.split(",").filter { it.isNotEmpty() }.map { JavaLanguageVersion.of(it) }
}
