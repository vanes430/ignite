/*
 * This file is part of Ignite, licensed under the MIT License (MIT).
 *
 * Copyright (c) vectrix.space <https://vectrix.space/>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package space.vectrix.ignite;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;
import space.vectrix.ignite.agent.IgniteAgent;
import space.vectrix.ignite.game.GameLocatorService;
import space.vectrix.ignite.game.GameProvider;
import space.vectrix.ignite.game.PaperGameLocator;
import space.vectrix.ignite.launch.ember.Ember;
import space.vectrix.ignite.mod.ModsImpl;

/**
 * Represents the main class which starts Ignite.
 *
 * @author vectrix
 * @since 1.0.0
 */
public final class IgniteBootstrap {
  static {
    try {
      loadRuntimeDependencies();
    } catch(final Exception exception) {
      Logger.error(exception, "Failed to load runtime dependencies");
      System.exit(1);
    }
  }

  private static IgniteBootstrap INSTANCE;

  /**
   * Returns the bootstrap instance.
   *
   * @return this instance
   * @since 1.0.0
   */
  public static @NotNull IgniteBootstrap instance() {
    return IgniteBootstrap.INSTANCE;
  }

  /**
   * The main entrypoint to start Ignite.
   *
   * @param arguments the launch arguments
   * @since 1.0.0
   */
  public static void main(final String@NotNull [] arguments) {
    new IgniteBootstrap().run(arguments);
  }

  private static void loadRuntimeDependencies() throws Exception {
    System.out.println("[Ignite] Loading runtime dependencies...");

    final dev.vankka.dependencydownload.path.DependencyPathProvider librariesPath =
      new dev.vankka.dependencydownload.path.DirectoryDependencyPathProvider(Paths.get("./cache/ignite"));

    final java.util.List<dev.vankka.dependencydownload.repository.Repository> repositories =
      java.util.Collections.singletonList(new dev.vankka.dependencydownload.repository.MavenRepository("https://repo.maven.apache.org/maven2"));

    System.out.println("[Ignite] Downloading dependencies from Maven Central...");
    final dev.vankka.dependencydownload.DependencyManager manager =
      new dev.vankka.dependencydownload.DependencyManager(librariesPath);
    manager.loadResource(
      dev.vankka.dependencydownload.resource.DependencyDownloadResource.parse(
        IgniteBootstrap.class.getResource("/runtimeDownload.txt")
      )
    );
    manager.downloadAll(java.util.concurrent.ForkJoinPool.commonPool(), repositories).join();

    System.out.println("[Ignite] Loading dependencies into classpath...");
    manager.loadAll(Runnable::run, path -> {
      System.out.println("[Ignite] Loaded: " + path.getFileName());
      try {
        IgniteAgent.addJar(path);
      } catch(final java.io.IOException e) {
        throw new RuntimeException(e);
      }
    }).join();

    System.out.println("[Ignite] Runtime dependencies loaded successfully");
  }

  private final ModsImpl engine;

  /* package */ IgniteBootstrap() {
    IgniteBootstrap.INSTANCE = this;
    this.engine = new ModsImpl();
  }

  private void run(final String@NotNull [] args) {
    final List<String> arguments = Arrays.asList(args);
    final List<String> launchArguments = new ArrayList<>(arguments);

    // Load configuration from ignite.json
    final space.vectrix.ignite.util.IgniteConfig config = space.vectrix.ignite.util.IgniteConfig.load();

    // Get a suitable game locator and game provider.
    final GameLocatorService gameLocator;
    final GameProvider gameProvider;
    {
      gameLocator = new PaperGameLocator();

      try {
        gameLocator.apply(this);
      } catch(final Throwable throwable) {
        final String message = "Failed to start game: Unable to apply GameLocator service.";
        Logger.error(throwable, message);
        System.exit(1);
        return;
      }

      gameProvider = gameLocator.locate();
    }

    Logger.info("Preparing the game...");

    // Add the game.
    final Path gameJar = gameProvider.gamePath();
    space.vectrix.ignite.util.ProtocolConstants.setGameJar(gameJar);
    try {
      IgniteAgent.addJar(gameJar);

      Logger.trace("Added game jar: {}", gameJar);
    } catch(final IOException exception) {
      Logger.error(exception, "Failed to resolve game jar: {}", gameJar);
      System.exit(1);
      return;
    }

    // Add the game libraries.
    gameProvider.gameLibraries().forEach(path -> {
      if(!path.toString().endsWith(".jar")) return;

      try {
        IgniteAgent.addJar(path);

        Logger.trace("Added game library jar: {}", path);
      } catch(final IOException exception) {
        Logger.error(exception, "Failed to resolve game library jar: {}", path);
      }
    });

    Logger.info("Launching the game...");

    // Initialize the API.
    Ignite.initialize(new PlatformImpl());

    // Launch the game.
    Ember.launch(launchArguments.toArray(new String[0]));
  }

  /**
   * Returns the mod engine.
   *
   * @return the mod engine
   * @since 1.0.0
   */
  public @NotNull ModsImpl engine() {
    return this.engine;
  }
}
