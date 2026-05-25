package com.github.vanes430.horizonlogin;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.byteflux.libby.BukkitLibraryManager;
import net.byteflux.libby.Library;
import org.bukkit.plugin.java.JavaPlugin;
import space.vectrix.ignite.util.HorizonLog;

import java.io.InputStream;
import java.io.InputStreamReader;

public final class LibraryLoader {

  public static void load(final JavaPlugin plugin) {
    final BukkitLibraryManager manager = new BukkitLibraryManager(plugin);
    manager.addMavenCentral();

    try (final InputStream is = plugin.getClass().getResourceAsStream("/library.json")) {
      if (is == null) {
        HorizonLog.error("library.json not found in jar!");
        return;
      }

      final JsonArray libs = new Gson().fromJson(new InputStreamReader(is), JsonArray.class);
      for (final JsonElement el : libs) {
        final JsonObject obj = el.getAsJsonObject();
        final Library lib = Library.builder()
          .groupId(obj.get("groupId").getAsString().replace(".", "{}"))
          .artifactId(obj.get("artifactId").getAsString())
          .version(obj.get("version").getAsString())
          .build();

        manager.loadLibrary(lib);
        HorizonLog.info("Loaded library: {}:{}:{}",
          obj.get("groupId").getAsString(),
          obj.get("artifactId").getAsString(),
          obj.get("version").getAsString());
      }
    } catch (final Exception e) {
      HorizonLog.error(e, "Failed to load runtime libraries");
    }
  }
}
