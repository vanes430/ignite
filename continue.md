# Continue - HorizonLogin Migration

## Project: C:\Users\ACER\IdeaProjects\horizonlogin
## Repo: https://github.com/vanes430/ignite.git (branch: master)

## What's Done
- Custom Ignite fork (Mixin loader for Paper)
- Per-protocol mixin system (767, 769, 772, 774, 775)
- Jar-in-jar plugin loading (no extraction)
- LoginEvent bus (per-player auth, UUID override, skin copy)
- ConfigurationEvent (pause/resume without timeout)
- /plugins Vanes section
- enforceSecureProfile bypass
- NMS UUID log suppression via Log4j2 filter
- HorizonLog (SLF4J + ANSI color)
- ignite.json config
- Libby runtime library system (library.json auto-generated)
- Config.java created (loads config.conf via configurate-hocon)
- config.conf default resource bundled

## What's Next (in order)
1. **Messages.java** - same pattern as Config, loads messages.conf
2. **Integrate Config + Messages** into HorizonLoginPlugin.onEnable()
3. **AuthManager** - HikariCP + SQLite, player table (uuid, name, password_hash, email, premium, last_ip, last_login)
4. **LoginState enum** - UNREGISTERED, AWAITING_LOGIN, AWAITING_REGISTER, AUTHENTICATED
5. **Connect LoginEventBus** - use Config.uuidHandler() to decide per-player auth
6. **Commands** - /login, /register, /premium, /cracked, /confirmcommand, /setemail, /confirmemail, /resetpassword, /horizonlogin (admin)
7. **Listeners** - block chat/commands/movement for unauthenticated players
8. **Limbo** - void world, freeze, bossbar timer
9. **Dialog** - modern 1.21.2+ dialog UI for login/register (via packetevents)
10. **Email** - jakarta.mail, verification codes, password reset

## Key Architecture
- Plugin classes in `plugin/src/main/java/com/github/vanes430/horizonlogin/`
- Plugin shaded into ignite.jar (jar-in-jar, no mods folder)
- Runtime libs downloaded by Libby at onLoad()
- Config files: `plugins/HorizonLogin/config.conf` and `plugins/HorizonLogin/messages.conf`
- Old source reference: `src/plugin/` (gitignored, not committed)
- Decompiled servers: `paper-api/` (gitignored)

## Dependencies (runtime via Libby)
- configurate-hocon 4.1.2
- configurate-core 4.1.2
- typesafe-config 1.4.3
- HikariCP 5.1.0
- sqlite-jdbc 3.49.1.0
- jakarta.mail 2.0.1
- jakarta.activation-api 2.1.3

## External plugins (in plugins/ folder)
- packetevents 2.12.1
- ViaVersion 5.9.1

## Supported Versions
- 1.21.1 (protocol 767)
- 1.21.4 (protocol 769)
- 1.21.8 (protocol 772)
- 1.21.11 (protocol 774)
- 26.1.2 (protocol 775)

## Build
```
.\gradlew.bat clean build
```
Output: `build/libs/ignite.jar`

## Run
```
java -jar ignite.jar -nogui
```
