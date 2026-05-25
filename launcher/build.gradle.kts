plugins {
  id("ignite.common-conventions")
  id("ignite.launcher-conventions")
  id("dev.vankka.dependencydownload.plugin") version "2.0.0"
}

dependencies {
  implementation(project(":ignite-api"))
  implementation("dev.vankka:dependencydownload-runtime:2.0.0")

  // Shade tinylog
  implementation(libs.tinylog.impl)

  // Shade gson (needed at bootstrap before runtime downloads are loaded)
  implementation(libs.gson)

  // Shade access-widener (needed by EmberClassLoader before runtime deps are visible)
  implementation(libs.accessWidener) {
    exclude(group = "org.ow2.asm")
  }

  // Shade ASM (needed before runtime downloads are loaded)
  implementation(libs.asm)
  implementation(libs.asm.analysis)
  implementation(libs.asm.commons)
  implementation(libs.asm.tree)
  implementation(libs.asm.util)

  // Runtime download dependencies
  runtimeDownload(libs.mixin) {
    exclude(group = "com.google.guava")
    exclude(group = "com.google.code.gson")
    exclude(group = "org.ow2.asm")
  }
  runtimeDownload(libs.mixinExtras) {
    exclude(group = "org.apache.commons")
  }
}

tasks {
  processResources {
    dependsOn(generateRuntimeDownloadResourceForRuntimeDownload)
  }
  shadowJar {
    dependsOn(generateRuntimeDownloadResourceForRuntimeDownload)
    dependsOn(":ignite-plugin:jar")
    doFirst {
      val pluginJar = project(":ignite-plugin").layout.buildDirectory.dir("libs").get().asFile
        .listFiles()?.firstOrNull { it.name.endsWith(".jar") }
      if (pluginJar != null) {
        from(zipTree(pluginJar))
      }
    }
  }
}
