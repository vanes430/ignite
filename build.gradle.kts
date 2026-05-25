plugins {
  alias(libs.plugins.indra.sonatype)
  alias(libs.plugins.nexusPublish)
  id("nl.littlerobots.version-catalog-update") version "1.1.0"
  id("com.gradleup.shadow") version "9.4.1"
}

versionCatalogUpdate {
  pin {
    versions.add("checkstyle")
    groups.add("com.gradleup.shadow")
    groups.add("net.fabricmc")
    groups.add("io.github.llamalad7")
  }
}

// Project metadata is configured in gradle.properties

tasks.register("clean", Delete::class) {
  description = ""
  delete(rootProject.layout.buildDirectory)
}
