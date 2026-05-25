<div align="center">
  <img src="./.github/ignite.png" width="250" height="250" alt="Ignite Logo">
  <br/><br/>
  <p><strong><a href="https://github.com/vectrix-space/ignite">Ignite</a></strong> is a <a href="https://github.com/SpongePowered/Mixin">Mixin</a> loader for Spigot/Paper.</p>
  <br/>
</div>

<div align="center">

![Build Status](https://github.com/vectrix-space/ignite/actions/workflows/build.yml/badge.svg)
[![MIT License](https://img.shields.io/badge/license-MIT-blue)](license.txt)
[![Discord](https://img.shields.io/discord/819522977586348052)](https://discord.gg/chpEj5UC45)
[![Maven Central](https://img.shields.io/maven-central/v/space.vectrix.ignite/ignite-api?label=stable)](https://search.maven.org/search?q=g:space.vectrix.ignite%20AND%20a:ignite*)
![Maven Snapshot Version](https://img.shields.io/badge/dynamic/xml?url=https%3A%2F%2Fcentral.sonatype.com%2Frepository%2Fmaven-snapshots%2Fspace%2Fvectrix%2Fignite%2Fignite-api%2Fmaven-metadata.xml&query=%2Fmetadata%2Fversioning%2Flatest&label=dev)

</div>

## Install

Download the `ignite.jar` from the [releases page](https://github.com/vectrix-space/ignite/releases/latest).

Place the `ignite.jar` into the same directory with your server jar (`paper.jar` or `server.jar`).

Run your server:
```bash
java -jar ignite.jar -nogui
```

No additional flags needed! Ignite will auto-detect `server.jar` and load all libraries.

The mods can then be placed into the `mods` directory that will be created.

## Making a Mod

The [ignite-mod-template](https://github.com/vectrix-space/ignite-mod-template) is a template you can use to start a project for Paper without needing to do all the setup yourself.

To depend on the Ignite API in order to create your mod, you will need to add the following to your buildscript:

#### Gradle
```groovy
repositories {
  mavenCentral()
  maven {
    url = "https://maven.fabricmc.net/"
  }
}

dependencies {
  compileOnly "space.vectrix.ignite:ignite-api:1.2.1"
  compileOnly "net.fabricmc:sponge-mixin:0.17.0+mixin.0.8.7"
  compileOnly "io.github.llamalad7:mixinextras-common:0.5.3"
}
```

<br/>

#### Maven
```xml
<repositories>
  <repository>
    <url>https://maven.fabricmc.net/</url>
  </repository>
</repositories>

<dependencies>
  <dependency>
    <groupId>space.vectrix.ignite</groupId>
    <artifactId>ignite-api</artifactId>
    <version>1.2.1</version>
  </dependency>
  <dependency>
    <groupId>net.fabricmc</groupId>
    <artifactId>sponge-mixin</artifactId>
    <version>0.17.0+mixin.0.8.7</version>
  </dependency>
  <dependency>
    <groupId>io.github.llamalad7</groupId>
    <artifactId>mixinextras-common</artifactId>
    <version>0.5.3</version>
  </dependency>
</dependencies>
```

**Note:** To support custom mappings you should check out [ignite-mod-template](https://github.com/vectrix-space/ignite-mod-template) 
if you're running Paper. For Spigot check out [Pacifist Remapper](https://github.com/PacifistMC/pacifist-remapper).

### Configuring your Mod

Your mod will require a `ignite.mod.json` in order to be located as a mod. The `ignite.mod.json` provides the metadata needed to load 
your mixins and access wideners.

Example `ignite.mod.json`:
```json
{
  "id": "example",
  "version": "1.0.0",
  "mixins": [
    "mixins.example.core.json"
  ],
  "wideners": [
    "example.accesswidener"
  ]
}
```

The mods will need to be placed in the directory the launcher will be targeting to load.

#### Using Mixins

The Mixin configuration files will need to be available in your mods binary in order to be loaded. The name of each configuration file 
should be added to the `mixins` section in your `ignite.mod.json`, or alternatively could be added to your jar manifest.

[Mixin Specification]

#### Using Access Wideners

The Access Wideners configuration files will need to be available in your mods binary in order to be loaded. The name of each 
configuration file should be added to the `wideners` section in your `ignite.mod.json`, or alternatively could be added to your 
jar manifest with the `AccessWidener` key.

**Warning:** Access wideners should only be used in situations where Mixin will not work!

[Access Widener Specification]

## Building
__Note:__ If you do not have [Gradle] installed then use `./gradlew` for Unix systems or Git Bash and gradlew.bat for Windows systems in 
place of any 'gradle' command.

In order to build Ignite you simply need to run the `gradle build` command. You can find the compiled JAR file in `./build/libs/` named 
'ignite.jar'.

## Inspiration

This project has many parts inspired by the following projects:

- [Orion]
- [Fabric]
- [Sponge]
- [Velocity]
- [plugin-spi]

[Mixin]: https://github.com/SpongePowered/Mixin
[Access Widener]: https://github.com/FabricMC/access-widener
[Mixin Specification]: https://github.com/SpongePowered/Mixin/wiki/Introduction-to-Mixins---The-Mixin-Environment#mixin-configuration-files
[Access Widener Specification]: https://fabricmc.net/wiki/tutorial:accesswideners

[Gradle]: https://www.gradle.org/
[Orion]: https://github.com/OrionMinecraft/Orion
[Fabric]: https://github.com/FabricMC/fabric-loader
[Sponge]: https://github.com/SpongePowered/Sponge
[Velocity]: https://github.com/VelocityPowered/Velocity
[plugin-spi]: https://github.com/SpongePowered/plugin-spi
