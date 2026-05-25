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

/**
 * Holds the detected protocol version, set early during boot.
 *
 * @author vanes430
 * @since 1.0.0
 */
public final class ProtocolConstants {
  private static int protocol = 0;
  private static java.nio.file.Path gameJar;

  /**
   * Set the protocol version (called by PaperGameLocator).
   *
   * @param value the protocol version
   * @since 1.0.0
   */
  public static void set(final int value) {
    protocol = value;
    System.setProperty("horizonlogin.protocol", String.valueOf(value));
  }

  /**
   * Get the protocol version.
   *
   * @return the protocol version
   * @since 1.0.0
   */
  public static int getProtocol() {
    if (protocol == 0) {
      final String prop = System.getProperty("horizonlogin.protocol");
      if (prop != null) {
        protocol = Integer.parseInt(prop);
      }
    }
    return protocol;
  }

  /**
   * Set the game jar path.
   *
   * @param path the game jar path
   * @since 1.0.0
   */
  public static void setGameJar(final java.nio.file.Path path) {
    gameJar = path;
  }

  /**
   * Get the game jar path.
   *
   * @return the game jar path
   * @since 1.0.0
   */
  public static java.nio.file.Path gameJar() {
    return gameJar;
  }

  private ProtocolConstants() {
  }
}
