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

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Event fired when a player enters the configuration phase.
 * Supports pause/resume to hold the player without timeout kick.
 *
 * @author vanes430
 * @since 1.0.0
 */
public final class ConfigurationEvent {
  private static final java.util.List<Consumer<ConfigurationEvent>> HANDLERS = new CopyOnWriteArrayList<>();
  private static final Set<String> PAUSED_PLAYERS = ConcurrentHashMap.newKeySet();

  private final String username;
  private boolean paused;

  /**
   * Creates a new configuration event.
   *
   * @param username the player username
   * @since 1.0.0
   */
  public ConfigurationEvent(final String username) {
    this.username = username;
    this.paused = false;
  }

  /**
   * Returns the player username.
   *
   * @return the username
   * @since 1.0.0
   */
  public String username() {
    return this.username;
  }

  /**
   * Pause the configuration phase. Player will not be kicked for timeout.
   *
   * @since 1.0.0
   */
  public void pause() {
    this.paused = true;
  }

  /**
   * Returns whether the configuration is paused.
   *
   * @return true if paused
   * @since 1.0.0
   */
  public boolean isPaused() {
    return this.paused;
  }

  /**
   * Register a configuration event handler.
   *
   * @param handler the handler
   * @since 1.0.0
   */
  public static void register(final Consumer<ConfigurationEvent> handler) {
    HANDLERS.add(handler);
  }

  /**
   * Fire the event for a player.
   *
   * @param username the username
   * @return true if paused
   * @since 1.0.0
   */
  public static boolean fire(final String username) {
    final ConfigurationEvent event = new ConfigurationEvent(username);
    for (final Consumer<ConfigurationEvent> handler : HANDLERS) {
      handler.accept(event);
    }
    if (event.isPaused()) {
      PAUSED_PLAYERS.add(username);
    }
    return event.isPaused();
  }

  /**
   * Check if a player is currently paused.
   *
   * @param username the username
   * @return true if paused
   * @since 1.0.0
   */
  public static boolean isPaused(final String username) {
    return PAUSED_PLAYERS.contains(username);
  }

  /**
   * Resume a paused player's configuration phase.
   *
   * @param username the username
   * @since 1.0.0
   */
  public static void resume(final String username) {
    PAUSED_PLAYERS.remove(username);
  }
}
