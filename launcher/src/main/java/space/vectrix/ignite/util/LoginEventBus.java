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

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Simple event bus for login events.
 *
 * @author vanes430
 * @since 1.0.0
 */
public final class LoginEventBus {
  private static final List<Consumer<LoginEvent>> HANDLERS = new CopyOnWriteArrayList<>();
  private static final Set<String> FORCE_OFFLINE_UUID = ConcurrentHashMap.newKeySet();
  private static final ThreadLocal<String> CURRENT_USERNAME = new ThreadLocal<>();

  /**
   * Register a login event handler.
   *
   * @param handler the handler
   * @since 1.0.0
   */
  public static void register(final Consumer<LoginEvent> handler) {
    HANDLERS.add(handler);
  }

  /**
   * Fire a login event to all handlers.
   *
   * @param event the event
   * @since 1.0.0
   */
  public static void fire(final LoginEvent event) {
    for (final Consumer<LoginEvent> handler : HANDLERS) {
      handler.accept(event);
    }
  }

  /**
   * Mark a username to use offline UUID.
   *
   * @param username the username
   * @since 1.0.0
   */
  public static void setForceOfflineUuid(final String username) {
    FORCE_OFFLINE_UUID.add(username);
  }

  /**
   * Check and consume whether a username should use offline UUID.
   *
   * @param username the username
   * @return true if offline uuid should be forced
   * @since 1.0.0
   */
  public static boolean shouldForceOfflineUuid(final String username) {
    return FORCE_OFFLINE_UUID.remove(username);
  }

  /**
   * Set the current username for the login flow (thread-local).
   *
   * @param username the username
   * @since 1.0.0
   */
  public static void setCurrentUsername(final String username) {
    CURRENT_USERNAME.set(username);
  }

  /**
   * Get the current username from the login flow.
   *
   * @return the username or null
   * @since 1.0.0
   */
  public static String getCurrentUsername() {
    return CURRENT_USERNAME.get();
  }

  /**
   * Clear the current username thread-local.
   *
   * @since 1.0.0
   */
  public static void clearCurrentUsername() {
    CURRENT_USERNAME.remove();
  }

  private LoginEventBus() {
  }
}
