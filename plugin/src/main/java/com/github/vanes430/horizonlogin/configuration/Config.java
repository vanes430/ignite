package com.github.vanes430.horizonlogin.configuration;

import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import space.vectrix.ignite.util.HorizonLog;

import java.nio.file.Files;
import java.nio.file.Path;

public final class Config {
  private final CommentedConfigurationNode root;

  private Config(final CommentedConfigurationNode root) {
    this.root = root;
  }

  public static Config load(final Path dataFolder) {
    try {
      final Path file = dataFolder.resolve("config.conf");
      if (!Files.exists(file)) {
        Files.createDirectories(dataFolder);
        Files.copy(Config.class.getResourceAsStream("/config.conf"), file);
      }

      final HoconConfigurationLoader loader = HoconConfigurationLoader.builder()
        .path(file)
        .build();

      return new Config(loader.load());
    } catch (final Exception e) {
      HorizonLog.error(e, "Failed to load config.conf");
      return new Config(null);
    }
  }

  public String uuidHandler() {
    return getString("uuid_handler", "CRACKED");
  }

  public boolean debug() {
    return getBoolean("debug", false);
  }

  public int minPasswordLength() {
    return getInt("min_password_length", 8);
  }

  public String databaseFile() {
    return getString("database.file", "players.db");
  }

  public String hashingMode() {
    return getString("database.hashing_mode", "BCRYPT");
  }

  public boolean sessionEnabled() {
    return getBoolean("session.enabled", true);
  }

  public int sessionTimeout() {
    return getInt("session.timeout", 10);
  }

  public boolean dialogEnabled() {
    return getBoolean("dialog.enabled", true);
  }

  public int dialogMaxAttempts() {
    return getInt("dialog.max_attempts", 3);
  }

  public boolean limboEnabled() {
    return getBoolean("limbo.enabled", true);
  }

  public String limboWorldName() {
    return getString("limbo.world_name", "limbo");
  }

  public int limboReminderDelay() {
    return getInt("limbo.reminder_delay", 3);
  }

  public int limboLoginTimeout() {
    return getInt("limbo.login_timeout", 60);
  }

  public boolean titlesEnabled() {
    return getBoolean("titles.enabled", true);
  }

  public boolean emailEnabled() {
    return getBoolean("email.enabled", false);
  }

  public String smtpHost() {
    return getString("email.smtp.host", "smtp.gmail.com");
  }

  public int smtpPort() {
    return getInt("email.smtp.port", 587);
  }

  public String smtpUsername() {
    return getString("email.smtp.username", "");
  }

  public String smtpPassword() {
    return getString("email.smtp.password", "");
  }

  public CommentedConfigurationNode node() {
    return this.root;
  }

  private String getString(final String path, final String def) {
    if (this.root == null) return def;
    try {
      final String[] parts = path.split("\\.");
      CommentedConfigurationNode node = this.root;
      for (final String part : parts) node = node.node(part);
      final String val = node.getString();
      return val != null ? val : def;
    } catch (final Exception e) {
      return def;
    }
  }

  private boolean getBoolean(final String path, final boolean def) {
    if (this.root == null) return def;
    try {
      final String[] parts = path.split("\\.");
      CommentedConfigurationNode node = this.root;
      for (final String part : parts) node = node.node(part);
      return node.getBoolean(def);
    } catch (final Exception e) {
      return def;
    }
  }

  private int getInt(final String path, final int def) {
    if (this.root == null) return def;
    try {
      final String[] parts = path.split("\\.");
      CommentedConfigurationNode node = this.root;
      for (final String part : parts) node = node.node(part);
      return node.getInt(def);
    } catch (final Exception e) {
      return def;
    }
  }
}
