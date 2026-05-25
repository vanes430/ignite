# Custom Patches (Mixin)

Dokumentasi semua mixin yang diterapkan oleh HorizonLogin melalui Ignite.

---

## 1. ServerLoginMixin

**Target:** `net.minecraft.server.network.ServerLoginPacketListenerImpl`

**Fungsi:**
- Intercept proses autentikasi saat player login
- Fire `LoginEvent` ke event bus sebelum keputusan auth dibuat
- Override UUID saat offline profile dibuat

**Tujuan:**
- Memungkinkan cracked player bergabung ke server `online-mode=true`
- Per-player auth control (bisa pilih siapa yang pakai Mojang auth, siapa yang skip)
- Custom UUID assignment agar data inventory/world tidak corrupt saat player cracked pakai UUID yang sama dengan Mojang UUID mereka

**Reward:**
- Server tetap bisa `online-mode=true` di server.properties (kompatibel dengan proxy, BungeeCord, dll)
- Plugin bisa decide per-player apakah perlu verifikasi Mojang atau tidak
- UUID konsisten antara cracked dan premium player (prevent data loss)

**Cross-version:** ✅ 1.21.1 (767), 1.21.8 (772), 1.21.11 (774), 26.1.2 (775)

---

## 2. PaperPluginsCommandMixin

**Target:** `io.papermc.paper.command.PaperPluginsCommand`

**Fungsi:**
- Inject di akhir method `execute` (baik versi legacy maupun brigadier)
- Menambahkan section "Vanes Plugins" di output `/plugins`

**Tujuan:**
- Menampilkan plugin yang di-load via jar-in-jar di bawah kategori terpisah
- Warna hijau = enabled, merah = error/disabled

**Reward:**
- Branding khusus untuk plugin Vanes
- Visibility plugin yang di-load non-konvensional (jar-in-jar)
- Terpisah dari Paper/Bukkit plugins section

**Cross-version:** ✅ Dual inject — legacy `execute(CommandSender, String, String[])` untuk 1.21.1, brigadier `execute(CommandContext)` untuk 1.21.8+

---

## 3. PluginInitializerManagerMixin

**Target:** `org.bukkit.craftbukkit.CraftServer`

**Fungsi:**
- Hook di `enablePlugins()` (HEAD, sekali jalan)
- Instantiate `HorizonLoginPlugin` langsung dari classpath tanpa file extraction
- Register dan enable plugin via `PaperPluginManagerImpl`

**Tujuan:**
- True jar-in-jar plugin loading — plugin class di-shade ke dalam ignite.jar
- Tidak ada file extraction ke disk
- Plugin ter-register di Bukkit plugin manager seperti plugin normal

**Reward:**
- Single file deployment (cukup ignite.jar)
- Plugin tidak bisa di-delete/modify oleh user (embedded)
- Tampil di Vanes Plugins section

**Cross-version:** ✅ `CraftServer.enablePlugins()` dan `PaperPluginManagerImpl.getInstance()` konsisten di semua versi

---

## Event Bus API

### LoginEventBus

```java
// Register handler (dari plugin onEnable)
LoginEventBus.register(event -> {
    // Skip Mojang auth untuk semua player
    event.setOnlineVerification(false);

    // Atau per-player
    if (isPremium(event.username())) {
        event.setOnlineVerification(true);
    } else {
        event.setOnlineVerification(false);
        // Set UUID dari database agar data tidak corrupt
        UUID uuid = getStoredUuid(event.username());
        if (uuid != null) {
            event.setUuid(uuid);
        }
    }
});
```

### VanesPluginRegistry

```java
// Register plugin ke /plugins output
VanesPluginRegistry.register("MyPlugin", true);  // hijau
VanesPluginRegistry.register("BrokenPlugin", false);  // merah
```

---

## Protocol Version Detection

Ignite membaca `version.json` dari server jar saat boot dan menyimpan protocol version di `Blackboard`:

```java
int protocol = Blackboard.raw(Blackboard.PROTOCOL_VERSION);
// 767 = 1.21.1, 772 = 1.21.8, 774 = 1.21.11, 775 = 26.1.2
```

Log output:
```
[main/INFO]: Detected protocol version: 775 (game: 26.1.2)
[main/INFO]: Applying embedded mixins from launcher jar: mixins.horizonlogin.json
```
