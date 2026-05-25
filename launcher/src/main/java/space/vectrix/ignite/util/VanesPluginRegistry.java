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

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for Vanes plugins shown in /plugins command.
 *
 * @author vanes430
 * @since 1.0.0
 */
public final class VanesPluginRegistry {
  private static final Map<String, Boolean> PLUGINS = new ConcurrentHashMap<>();

  /**
   * Register a plugin name with enabled status.
   *
   * @param name the plugin name
   * @param enabled whether the plugin is enabled
   * @since 1.0.0
   */
  public static void register(final String name, final boolean enabled) {
    PLUGINS.put(name, enabled);
  }

  /**
   * Returns the registered plugins with their status.
   *
   * @return unmodifiable map of name to enabled status
   * @since 1.0.0
   */
  public static Map<String, Boolean> plugins() {
    return Collections.unmodifiableMap(PLUGINS);
  }

  private VanesPluginRegistry() {
  }
}
