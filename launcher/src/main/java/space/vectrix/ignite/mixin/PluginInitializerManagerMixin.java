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

import java.io.File;
import java.lang.reflect.Method;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to load embedded plugins from ignite.jar via jar-in-jar.
 *
 * @author vanes430
 * @since 1.0.0
 */
@Mixin(targets = "org.bukkit.craftbukkit.CraftServer", remap = false)
public abstract class PluginInitializerManagerMixin {

  @Unique
  private static boolean horizonlogin$loaded = false;

  @Inject(method = "enablePlugins", at = @At("HEAD"))
  private void horizonlogin$loadEmbeddedPlugin(final CallbackInfo ci) {
    if (horizonlogin$loaded) return;
    horizonlogin$loaded = true;

    try {
      // Allocate plugin instance without calling constructor (bypasses classloader check)
      final Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
      final java.lang.reflect.Field unsafeField = unsafeClass.getDeclaredField("theUnsafe");
      unsafeField.setAccessible(true);
      final Object unsafe = unsafeField.get(null);

      final Class<?> pluginClass = Class.forName("com.github.vanes430.horizonlogin.HorizonLoginPlugin");
      final Object plugin = unsafeClass.getMethod("allocateInstance", Class.class).invoke(unsafe, pluginClass);

      // Create PluginDescriptionFile
      final Class<?> pdfClass = Class.forName("org.bukkit.plugin.PluginDescriptionFile");
      final Object pdf = pdfClass.getConstructor(String.class, String.class, String.class)
        .newInstance("HorizonLogin", "1.0.0", pluginClass.getName());

      // Set authors/contributors to avoid NPE in /version command
      final java.lang.reflect.Field authorsField = pdfClass.getDeclaredField("authors");
      authorsField.setAccessible(true);
      authorsField.set(pdf, java.util.List.of("vanes430"));
      final java.lang.reflect.Field contributorsField = pdfClass.getDeclaredField("contributors");
      contributorsField.setAccessible(true);
      contributorsField.set(pdf, java.util.List.of());

      // Get server and plugin manager
      final Class<?> bukkitClass = Class.forName("org.bukkit.Bukkit");
      final Object server = bukkitClass.getMethod("getServer").invoke(null);
      final Object pluginManager = bukkitClass.getMethod("getPluginManager").invoke(null);

      // Get a PluginLoader from an existing plugin
      final Object[] plugins = (Object[]) pluginManager.getClass().getMethod("getPlugins").invoke(pluginManager);
      Object loader = null;
      if (plugins.length > 0) {
        final Class<?> javaPluginClass = Class.forName("org.bukkit.plugin.java.JavaPlugin");
        final Method getLoaderMethod = javaPluginClass.getDeclaredMethod("getPluginLoader");
        getLoaderMethod.setAccessible(true);
        loader = getLoaderMethod.invoke(plugins[0]);
      }

      // Init JavaPlugin internals
      final Class<?> javaPluginClass = Class.forName("org.bukkit.plugin.java.JavaPlugin");
      final Class<?> pluginLoaderClass = Class.forName("org.bukkit.plugin.PluginLoader");
      final Class<?> serverClass = Class.forName("org.bukkit.Server");

      final Method initMethod = javaPluginClass.getDeclaredMethod("init", pluginLoaderClass, serverClass, pdfClass, File.class, File.class, ClassLoader.class);
      initMethod.setAccessible(true);

      final File dataFolder = new File("plugins/HorizonLogin");
      dataFolder.mkdirs();

      initMethod.invoke(plugin, loader, server, pdf, dataFolder, new File("cache/ignite/horizonlogin.jar"), pluginClass.getClassLoader());

      // Register plugin directly into the instance manager via PaperPluginManagerImpl
      final Class<?> paperPmClass = Class.forName("io.papermc.paper.plugin.manager.PaperPluginManagerImpl");
      final Method getInstanceMethod = paperPmClass.getMethod("getInstance");
      final Object paperPm = getInstanceMethod.invoke(null);

      // loadPlugin(Plugin) is on PaperPluginManagerImpl
      final Class<?> pluginInterface = Class.forName("org.bukkit.plugin.Plugin");
      paperPm.getClass().getMethod("loadPlugin", pluginInterface).invoke(paperPm, plugin);
      paperPm.getClass().getMethod("enablePlugin", pluginInterface).invoke(paperPm, plugin);

      space.vectrix.ignite.util.VanesPluginRegistry.register("HorizonLogin", true);
    } catch (final Throwable throwable) {
      java.util.logging.Logger.getLogger("HorizonLogin").severe("Failed to load embedded plugin: " + throwable);
    }
  }
}
