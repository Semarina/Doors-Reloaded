package de.jeff_media.doorsreloaded.config;

import de.jeff_media.doorsreloaded.DoorsReloadedMod;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Properties;

public class ModConfig {

    private static final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("doorsreloaded.properties").toFile();
    private static ModConfig INSTANCE;

    // Config fields
    public int config_version = 2;
    public boolean allow_doubledoors = true;
    public boolean check_for_redstone = true;
    public boolean allow_opening_irondoors_with_hands = false;
    public long autoclose = 0;
    public boolean allow_knocking = true;
    public boolean allow_knocking_trapdoors = false;
    public boolean knocking_requires_empty_hand = false;
    public boolean knocking_requires_shift = false;
    public String sound_knock_iron = "minecraft:entity.zombie.attack_iron_door";
    public String sound_knock_copper = "minecraft:entity.zombie.attack_iron_door";
    public String sound_knock_wood = "minecraft:item.shield.block";
    public double sound_knock_volume = 1.0;
    public double sound_knock_pitch = 1.0;
    public String sound_knock_category = "BLOCKS"; // Added category
    public boolean debug = false;
    public boolean check_for_updates = true;

    public static ModConfig getInstance() {
        if (INSTANCE == null) {
            load();
        }
        return INSTANCE;
    }

    public static void load() {
        INSTANCE = new ModConfig();
        if (CONFIG_FILE.exists()) {
            try (InputStreamReader reader = new InputStreamReader(new FileInputStream(CONFIG_FILE), StandardCharsets.UTF_8)) {
                Properties props = new Properties();
                props.load(reader);

                INSTANCE.config_version = getInt(props, "config-version", 2);
                INSTANCE.allow_doubledoors = getBoolean(props, "allow-doubledoors", true);
                INSTANCE.check_for_redstone = getBoolean(props, "check-for-redstone", true);
                INSTANCE.allow_opening_irondoors_with_hands = getBoolean(props, "allow-opening-irondoors-with-hands", false);
                INSTANCE.autoclose = getLong(props, "autoclose", 0L);
                INSTANCE.allow_knocking = getBoolean(props, "allow-knocking", true);
                INSTANCE.allow_knocking_trapdoors = getBoolean(props, "allow-knocking-trapdoors", false);
                INSTANCE.knocking_requires_empty_hand = getBoolean(props, "knocking-requires-empty-hand", false);
                INSTANCE.knocking_requires_shift = getBoolean(props, "knocking-requires-shift", false);
                INSTANCE.sound_knock_iron = props.getProperty("sound-knock-iron", "minecraft:entity.zombie.attack_iron_door");
                INSTANCE.sound_knock_copper = props.getProperty("sound-knock-copper", "minecraft:entity.zombie.attack_iron_door");
                INSTANCE.sound_knock_wood = props.getProperty("sound-knock-wood", "minecraft:item.shield.block");
                INSTANCE.sound_knock_volume = getDouble(props, "sound-knock-volume", 1.0);
                INSTANCE.sound_knock_pitch = getDouble(props, "sound-knock-pitch", 1.0);
                INSTANCE.sound_knock_category = props.getProperty("sound-knock-category", "BLOCKS");
                INSTANCE.debug = getBoolean(props, "debug", false);
                INSTANCE.check_for_updates = getBoolean(props, "check-for-updates", true);

                if (INSTANCE.config_version < 2) {
                    DoorsReloadedMod.LOGGER.info("Found outdated config (version " + INSTANCE.config_version + "), updating to version 2...");
                    INSTANCE.config_version = 2;
                    save();
                    DoorsReloadedMod.LOGGER.info("Config updated successfully.");
                }


            } catch (IOException e) {
                DoorsReloadedMod.LOGGER.error("Failed to load config", e);
            }
        } else {
            save(); // Save defaults
        }
    }

    public static void save() {
        // Manually writing to preserve comments and structure
        StringBuilder sb = new StringBuilder();

        sb.append("###########################\n");
        sb.append("#       DoorsReloaded     #\n");
        sb.append("###########################\n\n");

        sb.append("config-version: " + INSTANCE.config_version + "\n\n");

        sb.append("# Written by mfnalex / JEFF Media GbR\n");
        sb.append("# Community continuation of DoorsReloaded is maintained by Semarina organization.\n\n");

        sb.append("###########################\n");
        sb.append("#       Double doors      #\n");
        sb.append("###########################\n\n");

        sb.append("# When true, players can open-close double doors with right-click.\n");
        sb.append("# They need the permission \"doorsreloaded.doubledoors\" which is given to all players by default\n");
        sb.append("allow-doubledoors=" + INSTANCE.allow_doubledoors + "\n\n");

        sb.append("# Also open/close both doors when one door gets powered by redstone.\n");
        sb.append("# Note: if both doors are powered and only signal gets cut, only one door will close (like in vanilla)\n");
        sb.append("check-for-redstone=" + INSTANCE.check_for_redstone + "\n\n");

        sb.append("###########################\n");
        sb.append("#        Iron Doors       #\n");
        sb.append("###########################\n\n");

        sb.append("# When true, players can open-close iron doors and iron trapdoors with right-click.\n");
        sb.append("# They need the permission \"doorsreloaded.irondoors\".\n");
        sb.append("allow-opening-irondoors-with-hands=" + INSTANCE.allow_opening_irondoors_with_hands + "\n\n");

        sb.append("# Automatically closes iron doors and trapdoors after the given amount of seconds (0 to disable).\n");
        sb.append("autoclose=" + INSTANCE.autoclose + "\n\n");

        sb.append("###########################\n");
        sb.append("#         Knocking        #\n");
        sb.append("###########################\n\n");

        sb.append("# When true, players can knock on doors using left-click.\n");
        sb.append("# This only works for players in survival and adventure mode.\n");
        sb.append("# They need the permission \"doorsreloaded.knock\" which is given to all players by default\n");
        sb.append("allow-knocking=" + INSTANCE.allow_knocking + "\n\n");

        sb.append("# When true, players can knock to trapdoors like normal doors.\n");
        sb.append("# Players still need the permission \"doorsreloaded.knock\" which is given to all players by default\n");
        sb.append("# If allow-knocking is false, this feature won't work\n");
        sb.append("allow-knocking-trapdoors=" + INSTANCE.allow_knocking_trapdoors + "\n\n");

        sb.append("# When true, players can only knock when their hand is empty.\n");
        sb.append("knocking-requires-empty-hand=" + INSTANCE.knocking_requires_empty_hand + "\n\n");

        sb.append("# When true, players can only knock while sneaking\n");
        sb.append("knocking-requires-shift=" + INSTANCE.knocking_requires_shift + "\n\n");

        sb.append("# Settings for the knocking sound\n");
        sb.append("# See here: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Sound.html\n");
        sb.append("sound-knock-iron=" + INSTANCE.sound_knock_iron + "\n");
        sb.append("sound-knock-copper=" + INSTANCE.sound_knock_copper + "\n");
        sb.append("sound-knock-wood=" + INSTANCE.sound_knock_wood + "\n");
        sb.append("# A volume of 1.0 means 16 blocks, 2.0 means 32 blocks, etc.\n");
        sb.append("sound-knock-volume=" + INSTANCE.sound_knock_volume + "\n");
        sb.append("sound-knock-pitch=" + INSTANCE.sound_knock_pitch + "\n");
        sb.append("# See here: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/SoundCategory.html\n");
        sb.append("sound-knock-category=" + INSTANCE.sound_knock_category + "\n\n");

        sb.append("###########################\n");
        sb.append("#           Misc          #\n");
        sb.append("###########################\n\n");

        sb.append("# Debug mode\n");
        sb.append("debug=" + INSTANCE.debug + "\n\n");

        sb.append("# Check for updates on Modrinth\n");
        sb.append("check-for-updates=" + INSTANCE.check_for_updates + "\n");

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(CONFIG_FILE), StandardCharsets.UTF_8))) {
            writer.write(sb.toString());
        } catch (IOException e) {
            DoorsReloadedMod.LOGGER.error("Failed to save config", e);
        }
    }

    private static boolean getBoolean(Properties props, String key, boolean def) {
        String val = props.getProperty(key);
        if (val == null) return def;
        return Boolean.parseBoolean(val);
    }
    
    private static int getInt(Properties props, String key, int def) {
        String val = props.getProperty(key);
        if (val == null) return def;
        try { return Integer.parseInt(val); } catch (NumberFormatException e) { return def; }
    }

    private static long getLong(Properties props, String key, long def) {
        String val = props.getProperty(key);
        if (val == null) return def;
        try { return Long.parseLong(val); } catch (NumberFormatException e) { return def; }
    }

    private static double getDouble(Properties props, String key, double def) {
        String val = props.getProperty(key);
        if (val == null) return def;
        try { return Double.parseDouble(val); } catch (NumberFormatException e) { return def; }
    }
}
