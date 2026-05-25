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

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Helper to send Vanes Plugins section in /plugins output.
 *
 * @author vanes430
 * @since 1.0.0
 */
public final class VanesPluginsHelper {

  /**
   * Sends the Vanes Plugins section to console sender.
   *
   * @since 1.0.0
   */
  public static void send() {
    final Map<String, Boolean> vanesPlugins = VanesPluginRegistry.plugins();
    if (vanesPlugins.isEmpty()) return;

    try {
      final Class<?> bukkitClass = Class.forName("org.bukkit.Bukkit");
      final Object server = bukkitClass.getMethod("getServer").invoke(null);
      final Object consoleSender = server.getClass().getMethod("getConsoleSender").invoke(server);

      final Class<?> componentClass = Class.forName("net.kyori.adventure.text.Component");
      final Class<?> textColorClass = Class.forName("net.kyori.adventure.text.format.TextColor");
      final Class<?> namedTextColorClass = Class.forName("net.kyori.adventure.text.format.NamedTextColor");
      final Class<?> componentLikeClass = Class.forName("net.kyori.adventure.text.ComponentLike");

      final Method textMethod = componentClass.getMethod("text", String.class, textColorClass);
      final Method appendMethod = componentClass.getMethod("append", componentLikeClass);
      final Object vanesColor = textColorClass.getMethod("color", int.class).invoke(null, 0x8A2BE2);
      final Object green = namedTextColorClass.getField("GREEN").get(null);
      final Object red = namedTextColorClass.getField("RED").get(null);
      final Object darkGray = namedTextColorClass.getField("DARK_GRAY").get(null);

      final Object header = textMethod.invoke(null, "Vanes Plugins:", vanesColor);
      consoleSender.getClass().getMethod("sendMessage", componentClass).invoke(consoleSender, header);

      Object line = textMethod.invoke(null, " - ", darkGray);
      boolean first = true;
      for (final Map.Entry<String, Boolean> entry : vanesPlugins.entrySet()) {
        final Object color = entry.getValue() ? green : red;
        final Object name = textMethod.invoke(null, (first ? "" : ", ") + entry.getKey(), color);
        line = appendMethod.invoke(line, name);
        first = false;
      }

      consoleSender.getClass().getMethod("sendMessage", componentClass).invoke(consoleSender, line);
    } catch (final Throwable ignored) {
    }
  }

  private VanesPluginsHelper() {
  }
}
