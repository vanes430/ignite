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

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import space.vectrix.ignite.util.LoginEventBus;

/**
 * Mixin to intercept login authentication and apply UUID overrides.
 *
 * @author vanes430
 * @since 1.0.0
 */
@Mixin(targets = "net.minecraft.server.network.ServerLoginPacketListenerImpl", remap = false)
public abstract class ServerLoginMixin {

  @Shadow
  String requestedUsername;

  @Inject(method = "handleHello", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;usesAuthentication()Z"))
  private void horizonlogin$beforeAuth(final CallbackInfo ci) {
    LoginEventBus.setCurrentUsername(this.requestedUsername);
  }

  @Inject(method = "startClientVerification", at = @At("TAIL"))
  private void horizonlogin$overrideProfile(final CallbackInfo ci) {
    if (this.requestedUsername == null) return;
    if (!LoginEventBus.shouldForceOfflineUuid(this.requestedUsername)) return;

    try {
      final java.util.UUID offlineUuid = java.util.UUID.nameUUIDFromBytes(
        ("OfflinePlayer:" + this.requestedUsername).getBytes(java.nio.charset.StandardCharsets.UTF_8));
      final Class<?> gameProfileClass = Class.forName("com.mojang.authlib.GameProfile");
      final Object offlineProfile = gameProfileClass.getConstructor(java.util.UUID.class, String.class)
        .newInstance(offlineUuid, this.requestedUsername);

      final java.lang.reflect.Field profileField = this.getClass().getDeclaredField("authenticatedProfile");
      profileField.setAccessible(true);

      // Copy skin properties from original profile
      final Object originalProfile = profileField.get(this);
      if (originalProfile != null) {
        final Object originalProps = gameProfileClass.getMethod("getProperties").invoke(originalProfile);
        final Object newProps = gameProfileClass.getMethod("getProperties").invoke(offlineProfile);
        final Class<?> multimapClass = Class.forName("com.google.common.collect.Multimap");
        newProps.getClass().getMethod("putAll", multimapClass).invoke(newProps, originalProps);
      }

      profileField.set(this, offlineProfile);
      space.vectrix.ignite.util.HorizonLog.info(
        "UUID of player {} is {}", this.requestedUsername, offlineUuid);
    } catch (final Throwable ignored) {
    }
  }

  static {
    // Suppress NMS "UUID of player" log by adding a filter to the logger
    try {
      final Class<?> logManagerClass = Class.forName("org.apache.logging.log4j.LogManager");
      final Class<?> loggerClass = Class.forName("org.apache.logging.log4j.core.Logger");
      final Class<?> filterClass = Class.forName("org.apache.logging.log4j.core.Filter");

      final Object nmsLogger = logManagerClass.getMethod("getLogger", String.class)
        .invoke(null, "net.minecraft.server.network.ServerLoginPacketListenerImpl");

      // Create a custom filter via proxy
      final Object filter = java.lang.reflect.Proxy.newProxyInstance(
        filterClass.getClassLoader(),
        new Class<?>[]{filterClass},
        (proxy, method, args) -> {
          if ("filter".equals(method.getName()) && args != null) {
            for (final Object arg : args) {
              if (arg != null && arg.toString().contains("UUID of player")) {
                // Return DENY
                return Class.forName("org.apache.logging.log4j.core.Filter$Result")
                  .getField("DENY").get(null);
              }
            }
          }
          if ("getOnMatch".equals(method.getName()) || "getOnMismatch".equals(method.getName())) {
            return Class.forName("org.apache.logging.log4j.core.Filter$Result")
              .getField("NEUTRAL").get(null);
          }
          if ("getState".equals(method.getName())) {
            return Class.forName("org.apache.logging.log4j.core.LifeCycle$State")
              .getField("STARTED").get(null);
          }
          if ("isStarted".equals(method.getName())) return true;
          if ("isStopped".equals(method.getName())) return false;
          return null;
        }
      );

      loggerClass.getMethod("addFilter", filterClass).invoke(nmsLogger, filter);
    } catch (final Throwable ignored) {
    }
  }
}
