import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import dev.vankka.dependencydownload.task.GenerateDependencyDownloadResourceTask

plugins {
  id("com.gradleup.shadow")
}

var libs = extensions.getByType(org.gradle.accessors.dm.LibrariesForLibs::class)

val implementationVersion = project.version.toString()
val regexPattern = """(\d+\.\d+)""".toRegex()
val apiVersion = regexPattern.find(implementationVersion)?.value ?: "0.0"

// Relocations for shaded deps only (applied to shadowJar)
val igniteRelocations = listOf(
  "com.google.gson" to "space.vectrix.ignite.libs.gson",
  "net.fabricmc" to "space.vectrix.ignite.libs.fabricmc"
)
extra["igniteRelocations"] = igniteRelocations

tasks.getByName<Jar>("jar") {
  manifest {
    attributes(
      "Premain-Class" to "space.vectrix.ignite.agent.IgniteAgent",
      "Agent-Class" to "space.vectrix.ignite.agent.IgniteAgent",
      "Launcher-Agent-Class" to "space.vectrix.ignite.agent.IgniteAgent",
      "Main-Class" to "space.vectrix.ignite.IgniteBootstrap",
      "Multi-Release" to "true",

      "Specification-Title" to "ignite",
      "Specification-Version" to apiVersion,
      "Specification-Vendor" to "vectrix.space",

      "Implementation-Title" to project.name,
      "Implementation-Version" to implementationVersion,
      "Implementation-Vendor" to "vectrix.space"
    )

    attributes(
      "org/objectweb/asm/",
      "Implementation-Version" to libs.versions.asm
    )
  }
}

tasks.getByName<ShadowJar>("shadowJar") {
  mergeServiceFiles()
  igniteRelocations.forEach { (from, to) -> relocate(from, to) }
}

tasks.register<Copy>("dist") {
  group = "distribution"
  description = "Copies the shadow jar to the root build/libs as ignite.jar"

  val shadow = tasks.named<ShadowJar>("shadowJar")
  from(shadow)
  into(rootProject.layout.buildDirectory.dir("libs"))
  rename { "ignite.jar" }
  outputs.file(rootProject.layout.buildDirectory.file("libs/ignite.jar"))
}

tasks.getByName("build") {
  dependsOn("dist")
}
