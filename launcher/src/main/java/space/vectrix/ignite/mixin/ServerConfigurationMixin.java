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
package space.vectrix.ignite.mixin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import space.vectrix.ignite.util.ConfigurationEvent;

/**
 * Mixin to fire ConfigurationEvent and support pause/resume without timeout.
 *
 * @author vanes430
 * @since 1.0.0
 */
@Mixin(targets = "net.minecraft.server.network.ServerConfigurationPacketListenerImpl", remap = false)
public abstract class ServerConfigurationMixin {

  @Unique
  private String horizonlogin$username;

  @Unique
  private boolean horizonlogin$wasPaused;

  @Unique
  private boolean horizonlogin$eventFired;

  @Inject(method = "startConfiguration", at = @At("HEAD"), cancellable = true)
  private void horizonlogin$onStartConfiguration(final CallbackInfo ci) {
    if (this.horizonlogin$eventFired) return;
    this.horizonlogin$eventFired = true;

    try {
      final Field gpField = this.getClass().getDeclaredField("gameProfile");
      gpField.setAccessible(true);
      final Object profile = gpField.get(this);
      final Method getName = profile.getClass().getMethod("getName");
      this.horizonlogin$username = (String) getName.invoke(profile);

      space.vectrix.ignite.util.HorizonLog.info("ConfigurationEvent: player={}", this.horizonlogin$username);
      final boolean paused = ConfigurationEvent.fire(this.horizonlogin$username);

      if (paused) {
        space.vectrix.ignite.util.HorizonLog.info("Configuration paused for {}", this.horizonlogin$username);
        this.horizonlogin$wasPaused = true;
        ci.cancel();
      }
    } catch (final Throwable throwable) {
      space.vectrix.ignite.util.HorizonLog.error(throwable, "Failed to fire ConfigurationEvent");
    }
  }

  @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
  private void horizonlogin$onTick(final CallbackInfo ci) {
    if (this.horizonlogin$username == null) return;

    if (this.horizonlogin$wasPaused && !ConfigurationEvent.isPaused(this.horizonlogin$username)) {
      // Resumed! Call startConfiguration now
      space.vectrix.ignite.util.HorizonLog.info("Configuration resumed for {}", this.horizonlogin$username);
      this.horizonlogin$wasPaused = false;
      try {
        final Method startConfig = this.getClass().getDeclaredMethod("startConfiguration");
        startConfig.setAccessible(true);
        startConfig.invoke(this);
      } catch (final Throwable throwable) {
        space.vectrix.ignite.util.HorizonLog.error(throwable, "Failed to resume configuration");
      }
      return;
    }

    if (this.horizonlogin$wasPaused) {
      // Still paused - only keepalive, skip everything else
      try {
        final Method keepAlive = this.getClass().getSuperclass().getDeclaredMethod("keepConnectionAlive");
        keepAlive.setAccessible(true);
        keepAlive.invoke(this);
      } catch (final Throwable ignored) {
      }
      ci.cancel();
    }
  }
}
