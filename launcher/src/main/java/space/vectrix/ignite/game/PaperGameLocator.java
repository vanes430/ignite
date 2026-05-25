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
package space.vectrix.ignite.game;

import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;
import space.vectrix.ignite.IgniteBootstrap;
import space.vectrix.ignite.agent.IgniteAgent;
import space.vectrix.ignite.agent.transformer.PaperclipTransformer;
import space.vectrix.ignite.util.IgniteConstants;

/**
 * Provides a game locator for Paper.
 *
 * @author vectrix
 * @since 1.0.0
 */
public final class PaperGameLocator implements GameLocatorService {
  private static final String PAPER_TARGET = "io.papermc.paperclip.Paperclip";
  private static final Path GAME_LIBRARIES = Paths.get("./libraries");

  private PaperGameProvider provider;
  private Path serverJar;

  @Override
  public @NotNull String id() {
    return "paper";
  }

  @Override
  public @NotNull String name() {
    return "Paper";
  }

  @Override
  public int priority() {
    return 50;
  }

  @Override
  public boolean shouldApply() {
    Path path = space.vectrix.ignite.util.IgniteConfig.get().serverJar();

    // Try server.jar first
    if(!path.toFile().exists()) {
      path = Paths.get("./server.jar");
    }

    // Fall back to paper.jar if server.jar doesn't exist
    if(!path.toFile().exists()) {
      path = Paths.get("./paper.jar");
    }

    this.serverJar = path;

    try(final JarFile jarFile = new JarFile(path.toFile())) {
      // Read protocol early for mixin plugin
      final JarEntry versionEntry = jarFile.getJarEntry("version.json");
      if(versionEntry != null) {
        try(final InputStream is = jarFile.getInputStream(versionEntry)) {
          final JsonObject obj = IgniteConstants.GSON.fromJson(new InputStreamReader(is), JsonObject.class);
          if(obj.has("protocol_version")) {
            space.vectrix.ignite.util.ProtocolConstants.set(obj.getAsJsonPrimitive("protocol_version").getAsInt());
          }
        }
      }
      return versionEntry != null;
    } catch(final IOException exception) {
      return false;
    }
  }

  @Override
  public void apply(final @NotNull IgniteBootstrap bootstrap) throws Throwable {
    // Resolve server jar path
    if(this.serverJar == null) {
      Path path = space.vectrix.ignite.util.IgniteConfig.get().serverJar();
      if(!path.toFile().exists()) path = Paths.get("./server.jar");
      if(!path.toFile().exists()) path = Paths.get("./paper.jar");
      this.serverJar = path;
    }

    // Add the transformer to replace the system exits.
    IgniteAgent.addTransformer(new PaperclipTransformer(PAPER_TARGET.replace('.', '/')));

    // Set paperclip to patch only, we launch the game ourselves.
    System.setProperty("paperclip.patchonly", "true");

    // Add the paperclip jar.
    try {
      IgniteAgent.addJar(this.serverJar);
    } catch(final IOException exception) {
      throw new IllegalStateException("Unable to add paperclip jar to classpath!", exception);
    }

    // Run paperclip.
    try {
      final Class<?> paperclipClass = Class.forName(PAPER_TARGET);
      paperclipClass
        .getMethod("main", String[].class)
        .invoke(null, (Object) new String[0]);
    } catch(final ClassNotFoundException exception) {
      throw new IllegalStateException("Unable to execute paperclip jar!", exception);
    }

    // Create the game provider.
    if(this.provider == null) {
      this.provider = this.createProvider();
    }

    // Remove the patchonly flag.
    System.getProperties().remove("paperclip.patchonly");
  }

  @Override
  public @NotNull GameProvider locate() {
    return this.provider;
  }

  private PaperGameProvider createProvider() throws Throwable {
    // Extract game information from paperclip.
    final List<String> libraries = new ArrayList<>();
    String game = null;

    final Path path = this.serverJar;
    final File file = path.toFile();
    if(!file.exists()) throw new FileNotFoundException(file.getAbsolutePath());
    if(file.isDirectory() || !file.getName().endsWith(".jar")) throw new IOException("Provided path is not a jar file: " + path);

    try(final JarFile jarFile = new JarFile(file)) {
      // Determine where the game jar is located.
      {
        // Read the version.list for the game to launch.
        JarEntry entry = jarFile.getJarEntry("META-INF/versions.list");
        if(entry != null) {
          try(final InputStream inputStream = jarFile.getInputStream(entry); final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while((line = reader.readLine()) != null) {
              final String[] values = line.split("\t");

              if(values.length >= 3) {
                game = String.format("./versions/%s", values[2]);
                Logger.trace("Located paper jar from versions.list: {}", game);
                break;
              }
            }
          }
        }

        // Read the version.json if the version.list is not specifically set.
        entry = jarFile.getJarEntry("version.json");
        if(entry != null) {
          final InputStream inputStream = jarFile.getInputStream(entry);
          final JsonObject versionObject = IgniteConstants.GSON.fromJson(new InputStreamReader(inputStream), JsonObject.class);

          if(versionObject.has("protocol_version")) {
            final int protocol = versionObject.getAsJsonPrimitive("protocol_version").getAsInt();
            space.vectrix.ignite.util.ProtocolConstants.set(protocol);
            final String gameId = versionObject.has("id") ? versionObject.getAsJsonPrimitive("id").getAsString() : "unknown";

            // Supported protocols: 767(1.21.1), 769(1.21.4), 772(1.21.8), 774(1.21.11), 775(26.1.2)
            if(protocol != 767 && protocol != 769 && protocol != 772 && protocol != 774 && protocol != 775) {
              Logger.error("===========================================");
              Logger.error("UNSUPPORTED MINECRAFT VERSION: {} (protocol {})", gameId, protocol);
              Logger.error("HorizonLogin only supports: 1.21.1, 1.21.4, 1.21.8, 1.21.11, 26.1.2");
              Logger.error("===========================================");
              System.exit(1);
            }
          }

          if(game == null) {
            final String version = versionObject.getAsJsonPrimitive("id").getAsString();
            game = String.format("./versions/%s/paper-%s.jar", version, version);
            Logger.trace("Located paper jar from version.json: {}", game);
          }
        }

        if(game == null) {
          throw new IllegalStateException("Could not determine game jar from version.json!");
        }
      }

      // Read the libraries the game should launch with.
      {
        final JarEntry entry = jarFile.getJarEntry("META-INF/libraries.list");
        if(entry != null) {
          try(final InputStream inputStream = jarFile.getInputStream(entry); final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while((line = reader.readLine()) != null) {
              final String[] values = line.split("\t");

              if(values.length >= 3) {
                libraries.add(values[2]);
              }
            }
          }
        }
      }
    }

    return new PaperGameProvider(game, libraries);
  }

  /* package */ static final class PaperGameProvider implements GameProvider {
    private final List<String> libraries;
    private final String game;

    /* package */ PaperGameProvider(final @NotNull String game, final @NotNull List<String> libraries) {
      this.game = game;
      this.libraries = libraries;
    }

    @Override
    public @NotNull Stream<Path> gameLibraries() {
      final Path libraryPath = GAME_LIBRARIES;
      return this.libraries.stream().map(libraryPath::resolve);
    }

    @Override
    public @NotNull Path gamePath() {
      return Paths.get(this.game);
    }
  }
}
