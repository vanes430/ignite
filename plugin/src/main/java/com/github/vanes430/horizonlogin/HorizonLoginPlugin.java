package com.github.vanes430.horizonlogin;

import org.bukkit.plugin.java.JavaPlugin;
import space.vectrix.ignite.util.ConfigurationEvent;
import space.vectrix.ignite.util.HorizonLog;
import space.vectrix.ignite.util.LoginEventBus;

public final class HorizonLoginPlugin extends JavaPlugin {

  @Override
  public void onLoad() {
    // Load runtime libraries via Libby
    LibraryLoader.load(this);
  }

  @Override
  public void onEnable() {
    HorizonLog.info("Loaded via jar-in-jar from ignite.jar");

    LoginEventBus.register(event -> {
      HorizonLog.info("========== LoginEvent Fired ==========");
      HorizonLog.info("Username: {}", event.username());
      HorizonLog.info("Online Verification (default): {}", event.useOnlineVerification());
      HorizonLog.info("Use Mojang UUID: {}", event.useMojangUuid());
      HorizonLog.info("=======================================");

      if (event.username().equalsIgnoreCase("vanes430")) {
        event.setOnlineVerification(true);
        event.setUseMojangUuid(false);
        HorizonLog.info(">> vanes430: online verification = true, useMojangUuid = false");
      } else {
        event.setOnlineVerification(false);
        HorizonLog.info(">> {}: online verification = false (cracked allowed)", event.username());
      }
    });

    ConfigurationEvent.register(event -> {
      HorizonLog.info("========== ConfigurationEvent Fired ==========");
      HorizonLog.info("Username: {}", event.username());
      HorizonLog.info(">> Pausing configuration for 20 seconds...");
      event.pause();

      new Thread(() -> {
        try {
          Thread.sleep(20000);
        } catch (InterruptedException ignored) {
        }
        HorizonLog.info(">> Resuming configuration for {}", event.username());
        ConfigurationEvent.resume(event.username());
      }, "HorizonLogin-Resume-" + event.username()).start();
    });
  }

  @Override
  public void onDisable() {
  }
}
