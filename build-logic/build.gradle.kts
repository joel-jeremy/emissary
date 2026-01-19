plugins {
  `kotlin-dsl`
}

dependencies {
  /**
   * Workaround for accessing the version catalog from the build-logic project.
   *
   * See https://github.com/gradle/gradle/issues/15383
   */
  implementation(libs.nexuspublish.plugin)
  implementation(libs.snyk.plugin)
  implementation(libs.sonarqube.plugin)
  implementation(libs.errorprone.plugin)
  implementation(libs.nullaway.plugin)
  implementation(libs.spotless.plugin)
  implementation(libs.dependencyupdates.plugin)
  implementation(libs.jmh.plugin)
  implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}
