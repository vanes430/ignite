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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import space.vectrix.ignite.util.LoginEvent;
import space.vectrix.ignite.util.LoginEventBus;

/**
 * Mixin to intercept usesAuthentication and apply per-player auth decision.
 *
 * @author vanes430
 * @since 1.0.0
 */
@Mixin(targets = "net.minecraft.server.MinecraftServer", remap = false)
public abstract class MinecraftServerMixin {

  @Shadow
  private boolean onlineMode;

  @Inject(method = "usesAuthentication", at = @At("HEAD"), cancellable = true)
  private void horizonlogin$perPlayerAuth(final CallbackInfoReturnable<Boolean> cir) {
    final String username = LoginEventBus.getCurrentUsername();
    if (username == null) return;

    LoginEventBus.clearCurrentUsername();

    final LoginEvent event = new LoginEvent(username, this.onlineMode);
    LoginEventBus.fire(event);

    if (!event.useMojangUuid()) {
      LoginEventBus.setForceOfflineUuid(username);
    }

    cir.setReturnValue(event.useOnlineVerification());
  }

  @Inject(method = "enforceSecureProfile", at = @At("HEAD"), cancellable = true)
  private void horizonlogin$bypassSecureProfile(final CallbackInfoReturnable<Boolean> cir) {
    cir.setReturnValue(false);
  }
}
