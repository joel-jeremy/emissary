plugins {
  id("java")
}

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(25)
  }
}

tasks.withType<JavaCompile>().configureEach {
  options.release = 11
}

tasks.withType<Javadoc>().configureEach {
  (options as StandardJavadocDocletOptions).tags(
    "apiNote:a:API Note:",
    "implSpec:a:Implementation Requirements:",
    "implNote:a:Implementation Note:"
  )
}