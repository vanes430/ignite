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
package space.vectrix.ignite.util;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Configuration loaded from ignite.json.
 *
 * @author vanes430
 * @since 1.0.0
 */
public final class IgniteConfig {
  private static final Path CONFIG_PATH = Paths.get("./ignite.json");
  private static final Gson GSON = new Gson();
  private static IgniteConfig instance;

  @SerializedName("debug")
  private boolean debug = false;

  @SerializedName("server_jar")
  private String serverJar = "./server.jar";

  /**
   * Load or create the config.
   *
   * @return the config instance
   * @since 1.0.0
   */
  public static IgniteConfig load() {
    if (instance != null) return instance;

    if (Files.exists(CONFIG_PATH)) {
      try (final Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
        instance = GSON.fromJson(reader, IgniteConfig.class);
      } catch (final IOException exception) {
        instance = new IgniteConfig();
      }
    } else {
      instance = new IgniteConfig();
      instance.save();
    }

    return instance;
  }

  /**
   * Returns the config instance.
   *
   * @return the config
   * @since 1.0.0
   */
  public static IgniteConfig get() {
    return instance != null ? instance : load();
  }

  /**
   * Save config to disk.
   *
   * @since 1.0.0
   */
  public void save() {
    try (final Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
      GSON.toJson(this, writer);
    } catch (final IOException ignored) {
    }
  }

  /**
   * Returns whether debug mode is enabled.
   *
   * @return true if debug
   * @since 1.0.0
   */
  public boolean debug() {
    return this.debug;
  }

  /**
   * Returns the server jar path.
   *
   * @return the server jar path
   * @since 1.0.0
   */
  public Path serverJar() {
    return Paths.get(this.serverJar);
  }
}
