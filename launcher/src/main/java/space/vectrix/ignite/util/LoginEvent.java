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
 * Event fired before authentication decision for a connecting player.
 *
 * @author vanes430
 * @since 1.0.0
 */
public final class LoginEvent {
  private final String username;
  private boolean onlineVerification;
  private boolean useMojangUuid;

  /**
   * Creates a new login event.
   *
   * @param username the player username
   * @param defaultOnline the server's default online mode
   * @since 1.0.0
   */
  public LoginEvent(final String username, final boolean defaultOnline) {
    this.username = username;
    this.onlineVerification = defaultOnline;
    this.useMojangUuid = defaultOnline;
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
   * Returns whether online verification should be used.
   *
   * @return true if online verification is enabled
   * @since 1.0.0
   */
  public boolean useOnlineVerification() {
    return this.onlineVerification;
  }

  /**
   * Sets whether to use Mojang online verification for this player.
   *
   * @param online true to verify with Mojang, false to skip
   * @since 1.0.0
   */
  public void setOnlineVerification(final boolean online) {
    this.onlineVerification = online;
  }

  /**
   * Returns whether to use Mojang UUID (true) or offline UUID from name (false).
   *
   * @return true if using mojang uuid
   * @since 1.0.0
   */
  public boolean useMojangUuid() {
    return this.useMojangUuid;
  }

  /**
   * Sets whether to use Mojang UUID or generate offline UUID from name.
   *
   * @param useMojang true for mojang uuid, false for offline uuid
   * @since 1.0.0
   */
  public void setUseMojangUuid(final boolean useMojang) {
    this.useMojangUuid = useMojang;
  }
}
