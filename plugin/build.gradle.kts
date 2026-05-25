plugins {
  java
}

java {
  sourceCompatibility = JavaVersion.VERSION_21
  targetCompatibility = JavaVersion.VERSION_21
}

val libs = extensions.getByType(org.gradle.accessors.dm.LibrariesForLibs::class)

dependencies {
  compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
  compileOnly(project(":ignite-launcher"))

  // Libby shaded into plugin (small footprint)
  implementation(libs.libby.bukkit)

  // CompileOnly: runtime libs downloaded by Libby at runtime
  compileOnly(libs.configurate.hocon)
  compileOnly(libs.configurate.core)
  compileOnly(libs.hikaricp)
  compileOnly(libs.sqlite.jdbc)
  compileOnly(libs.jakarta.mail)
}

// Generate library.json from version catalog for Libby to download at runtime
tasks.register("generateLibraryJson") {
  val outputFile = layout.buildDirectory.file("resources/main/library.json")
  outputs.file(outputFile)

  doLast {
    val libraries = listOf(
      libs.configurate.hocon.get(),
      libs.configurate.core.get(),
      libs.typesafe.config.get(),
      libs.hikaricp.get(),
      libs.sqlite.jdbc.get(),
      libs.jakarta.mail.get(),
      libs.jakarta.activation.get()
    )

    val json = buildString {
      appendLine("[")
      libraries.forEachIndexed { index, dep ->
        val group = dep.module.group
        val name = dep.module.name
        val version = dep.versionConstraint.requiredVersion
        append("  {\"groupId\":\"$group\",\"artifactId\":\"$name\",\"version\":\"$version\"}")
        if (index < libraries.size - 1) appendLine(",") else appendLine()
      }
      appendLine("]")
    }

    outputFile.get().asFile.parentFile.mkdirs()
    outputFile.get().asFile.writeText(json)
  }
}

tasks.named("processResources") {
  dependsOn("generateLibraryJson")
}
