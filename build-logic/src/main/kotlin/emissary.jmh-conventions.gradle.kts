plugins {
  id("me.champeau.jmh")
}

dependencies {
  jmh("org.springframework:spring-context:5.3.39")
  jmh("net.sizovs:pipelinr:0.11")
  jmh("org.greenrobot:eventbus-java:3.3.1")
  jmh("com.google.guava:guava:33.5.0-jre")
  jmh("net.engio:mbassador:1.3.2")
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
