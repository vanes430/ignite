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

/**
 * Custom logging utility for HorizonLogin.
 * Uses SLF4J with colored prefix in message.
 *
 * @author vanes430
 * @since 1.0.0
 */
public final class HorizonLog {
  private static final String COLOR_PREFIX = "\u001B[92m[HorizonLogin]\u001B[0m ";
  private static Object logger;
  private static Method infoMethod;
  private static Method warnMethod;
  private static Method errorMethod;

  static {
    try {
      final Class<?> factory = Class.forName("org.slf4j.LoggerFactory");
      final Class<?> loggerClass = Class.forName("org.slf4j.Logger");
      logger = factory.getMethod("getLogger", String.class).invoke(null, "");
      infoMethod = loggerClass.getMethod("info", String.class, Object[].class);
      warnMethod = loggerClass.getMethod("warn", String.class, Object[].class);
      errorMethod = loggerClass.getMethod("error", String.class, Object[].class);
    } catch (final Throwable ignored) {
      logger = null;
    }
  }

  /**
   * Log an info message.
   *
   * @param msg the message
   * @param args the arguments (replaces {} in order)
   * @since 1.0.0
   */
  public static void info(final String msg, final Object... args) {
    log(infoMethod, msg, args);
  }

  /**
   * Log a warning message.
   *
   * @param msg the message
   * @param args the arguments
   * @since 1.0.0
   */
  public static void warn(final String msg, final Object... args) {
    log(warnMethod, msg, args);
  }

  /**
   * Log an error message.
   *
   * @param msg the message
   * @param args the arguments
   * @since 1.0.0
   */
  public static void error(final String msg, final Object... args) {
    log(errorMethod, msg, args);
  }

  /**
   * Log an error message with throwable.
   *
   * @param throwable the throwable
   * @param msg the message
   * @param args the arguments
   * @since 1.0.0
   */
  public static void error(final Throwable throwable, final String msg, final Object... args) {
    final Object[] argsWithThrowable = new Object[args.length + 1];
    System.arraycopy(args, 0, argsWithThrowable, 0, args.length);
    argsWithThrowable[args.length] = throwable;
    log(errorMethod, msg, argsWithThrowable);
  }

  private static void log(final Method method, final String msg, final Object... args) {
    if (logger != null) {
      try {
        method.invoke(logger, COLOR_PREFIX + msg, args);
        return;
      } catch (final Throwable ignored) {
      }
    }
    // Fallback
    System.out.println("[HorizonLogin] " + format(msg, args));
  }

  private static String format(final String msg, final Object... args) {
    String formatted = msg;
    for (final Object arg : args) {
      final int idx = formatted.indexOf("{}");
      if (idx == -1) break;
      formatted = formatted.substring(0, idx) + arg + formatted.substring(idx + 2);
    }
    return formatted;
  }

  private HorizonLog() {
  }
}
