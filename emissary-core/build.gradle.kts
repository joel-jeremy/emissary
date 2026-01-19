plugins {
  id("emissary.java-library-conventions")
  id("emissary.java-multi-jvm-test-conventions")
  id("emissary.java-testing-conventions")
  id("emissary.java-code-quality-conventions")
  id("emissary.java-publish-conventions")
  id("emissary.eclipse-conventions")
  id("emissary.jmh-conventions")
}

description = "Emissary Core"

tasks.named<Jar>("jar") {
  manifest {
    attributes(mapOf(
      "Automatic-Module-Name" to "io.github.joeljeremy.emissary.core"
    ))
  }
}
