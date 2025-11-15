# DoorsReloaded
DoorsReloaded modernises door interactions with lightweight quality-of-life features for modern Paper-family servers.

## Supported servers
- ✅ **Paper** 1.21+
- ✅ **Purpur** 1.21+
- ✅ **Folia** 1.21+ (region-threading aware)
- ❌ Spigot / CraftBukkit (not supported)
- ❌ Sponge or other non-Paper platforms (not supported)

## Folia awareness
The plugin detects Folia at runtime and uses a scheduler abstraction that delegates door updates to Folia's region scheduler, ensuring all world interactions occur on the correct region threads.

## Building
DoorsReloaded uses Maven. To build the shaded plugin JAR, run:

```bash
mvn clean package
```

The resulting artifact is located at `target/DoorsReloaded.jar`.