plugins {
  id("me.champeau.jmh")
}

dependencies {
  jmh("org.springframework:spring-context:5.3.39")
  jmh("net.sizovs:pipelinr:0.11")
  jmh("org.greenrobot:eventbus-java:3.3.1")
}

val benchmarksFolderPath = "src/jmh/java/io/github/joeljeremy/emissary/core/benchmarks"

jmh {
  jmhVersion = "1.37"
  humanOutputFile = layout.buildDirectory.file("reports/jmh/human.txt")
  resultsFile = layout.projectDirectory.file("${benchmarksFolderPath}/results-java${JavaVersion.current().majorVersion}.json")
  resultFormat = "JSON"
  jvmArgs.addAll(listOf("-Xmx2G"))

  javaLauncher = javaToolchains.launcherFor {
    languageVersion = JavaLanguageVersion.of(JavaVersion.current().majorVersion)
  }
}

/**
 * Ideally every LTS release (succeeding the version used in source compilation) 
 * plus the latest released non-LTS version.
 */
fun additionalJmhRunsOnJvmVersions(): List<JavaLanguageVersion> {
  // 25 is enabled by default (Default java-conventions toolchain is 25)
  val defaultJvmVersions = "11,17,21"
  val jvmVersions = findProperty("additionalJmhRunsOnJvmVersions") as String?
      ?: defaultJvmVersions
  return jvmVersions.split(",").filter { it.isNotEmpty() }.map { JavaLanguageVersion.of(it) }
}
