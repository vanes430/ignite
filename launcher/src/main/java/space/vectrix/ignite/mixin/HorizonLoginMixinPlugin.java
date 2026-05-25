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

import java.util.List;
import java.util.Set;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

/**
 * Mixin config plugin that enables/disables mixin configs based on protocol version.
 *
 * <p>Config file naming convention:</p>
 * <ul>
 *   <li>{@code mixins.horizonlogin.json} - applied to ALL versions</li>
 *   <li>{@code mixins.horizonlogin.v767.json} - applied only to protocol 767 (1.21.1)</li>
 *   <li>{@code mixins.horizonlogin.v769.json} - applied only to protocol 769 (1.21.4)</li>
 *   <li>{@code mixins.horizonlogin.v772.json} - applied only to protocol 772 (1.21.8)</li>
 * </ul>
 *
 * @author vanes430
 * @since 1.0.0
 */
public final class HorizonLoginMixinPlugin implements IMixinConfigPlugin {
  private int targetProtocol = -1;

  @Override
  public void onLoad(final String mixinPackage) {
    // Extract protocol from config name: mixins.horizonlogin.v767.json -> 767
    // If no version suffix, targetProtocol stays -1 (apply to all)
    final String configName = mixinPackage;
    // mixinPackage is actually the package, not config name
    // We'll determine from the package name instead
  }

  @Override
  public String getRefMapperConfig() {
    return null;
  }

  @Override
  public boolean shouldApplyMixin(final String targetClassName, final String mixinClassName) {
    final int protocol = space.vectrix.ignite.util.ProtocolConstants.getProtocol();
    boolean result = true;

    // Single protocol: space.vectrix.ignite.mixin.v767.SomeClass
    if (mixinClassName.contains(".mixin.v767.")) result = protocol == 767;
    else if (mixinClassName.contains(".mixin.v769.")) result = protocol == 769;
    else if (mixinClassName.contains(".mixin.v772.")) result = protocol == 772;
    else if (mixinClassName.contains(".mixin.v774.")) result = protocol == 774;
    else if (mixinClassName.contains(".mixin.v775.")) result = protocol == 775;
    // Range: space.vectrix.ignite.mixin.v767_769.SomeClass
    else if (mixinClassName.contains(".mixin.v767_769.")) result = protocol >= 767 && protocol <= 769;
    else if (mixinClassName.contains(".mixin.v772_775.")) result = protocol >= 772 && protocol <= 775;

    return result;
  }

  @Override
  public void acceptTargets(final Set<String> myTargets, final Set<String> otherTargets) {
  }

  @Override
  public List<String> getMixins() {
    return null;
  }

  @Override
  public void preApply(final String targetClassName, final ClassNode targetClass, final String mixinClassName, final IMixinInfo mixinInfo) {
  }

  @Override
  public void postApply(final String targetClassName, final ClassNode targetClass, final String mixinClassName, final IMixinInfo mixinInfo) {
  }
}
