plugins {
  id("net.kyori.indra")
}

var libs = extensions.getByType(org.gradle.accessors.dm.LibrariesForLibs::class)

indra {
  javaVersions {
    minimumToolchain(21)
    target(21)
  }

  checkstyle(libs.versions.checkstyle.get())

  github("vectrix-space", "ignite") {
    ci(true)
  }

  mitLicense()
}
