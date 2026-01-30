import me.champeau.jmh.JMHTask;

plugins {
  id("me.champeau.jmh")
}

dependencies {
  jmh("org.springframework:spring-context:5.3.39")
  jmh("net.sizovs:pipelinr:0.11")
  jmh("org.greenrobot:eventbus-java:3.3.1")
  jmh("com.google.guava:guava:33.5.0-jre")
  jmh("net.engio:mbassador:1.3.2")
  jmh("io.reactivex.rxjava3:rxjava:3.1.12")
  jmh("com.squareup:otto:1.3.8")
  jmh("org.jboss.weld.se:weld-se-shaded:6.0.4.Final")
}

val commonJmhVersion = "1.37"
val commonResultFormat = "JSON"
val commonJvmArgs = listOf("-Xmx2G")

fun resultsFilePath(javaVersion: JavaLanguageVersion): String {
  val benchmarksFolderPath = "src/jmh/java/io/github/joeljeremy/emissary/core/benchmarks"
  return "${benchmarksFolderPath}/results-java${javaVersion}.json"
}

fun humanOutputFilePath(javaVersion: JavaLanguageVersion): String = "reports/jmh/human-java${javaVersion}.txt"

jmh {
  jmhVersion = commonJmhVersion
  resultFormat = commonResultFormat
  jvmArgs.addAll(commonJvmArgs)

  humanOutputFile = layout.buildDirectory.file(humanOutputFilePath(JavaLanguageVersion.of(JavaVersion.current().majorVersion)))
  resultsFile = layout.projectDirectory.file(resultsFilePath(JavaLanguageVersion.of(JavaVersion.current().majorVersion)))
}

additionalJmhRunsOnJvmVersions().forEach { additionalJavaVersion ->
  val jmhTaskName = "jmhOnJava${additionalJavaVersion}"

  tasks.register<JMHTask>(jmhTaskName) {
    jmhVersion = commonJmhVersion
    resultFormat = commonResultFormat
    jvmArgs.addAll(commonJvmArgs)
    
    javaLauncher = javaToolchains.launcherFor {
      languageVersion = additionalJavaVersion
    }

    humanOutputFile = layout.buildDirectory.file(humanOutputFilePath(additionalJavaVersion))
    resultsFile = layout.projectDirectory.file(resultsFilePath(additionalJavaVersion))
    
    jarArchive = tasks.named<Jar>("jmhJar").flatMap { it.archiveFile }
  }
}

/**
 * Ideally every LTS release (succeeding the version used in source compilation) 
 * plus the latest released non-LTS version.
 */
fun additionalJmhRunsOnJvmVersions(): List<JavaLanguageVersion> {
  // 25 is enabled by default (Default java-conventions toolchain is 25)
  val defaultJvmVersions = "17,21"
  val jvmVersions = findProperty("additionalJmhRunsOnJvmVersions") as String?
      ?: defaultJvmVersions
  return jvmVersions.split(",").filter { it.isNotEmpty() }.map { JavaLanguageVersion.of(it) }
}
