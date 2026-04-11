package org.bestraxstudio.playerrooms.config;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.util.*;

public class ConfigManager {

    private final JavaPlugin plugin;
    private YamlConfiguration config;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) plugin.saveResource("config.yml", false);
        this.config = YamlConfiguration.loadConfiguration(configFile);
    }

    public void reload() { loadConfig(); }
    public YamlConfiguration getConfig() { return config; }

    public String getAdminPermission() { return config.getString("permissions.admin", "playerrooms.admin"); }
    public String getForceJoinPermission() { return config.getString("permissions.force-join", "playerrooms.forcejoin"); }

    public String getWorldName() {
        return config.getString("world", "world");
    }
    public Set<String> getRoomNames() {
        ConfigurationSection roomsSection = config.getConfigurationSection("rooms");
        return roomsSection != null ? roomsSection.getKeys(false) : new HashSet<>();
    }

    public Location getRoomSpawnPoint(String roomName) {
        String spawnStr = config.getString("rooms." + roomName + ".spawn-point", "0 64 0");
        String[] parts = spawnStr.split(" ");
        if (parts.length < 3) return null;

        World world = Bukkit.getWorld(getWorldName());
        if (world == null) world = Bukkit.getWorlds().get(0);

        double x = Double.parseDouble(parts[0]);
        double y = Double.parseDouble(parts[1]);
        double z = Double.parseDouble(parts[2]);
        float yaw = parts.length >= 5 ? Float.parseFloat(parts[3]) : 0;
        float pitch = parts.length >= 5 ? Float.parseFloat(parts[4]) : 0;

        return new Location(world, x, y, z, yaw, pitch);
    }

    public boolean hasRoomBounds(String roomName) {
        return config.contains("rooms." + roomName + ".min") && config.contains("rooms." + roomName + ".max");
    }

    public Location getRoomMin(String roomName) {
        String minStr = config.getString("rooms." + roomName + ".min");
        if (minStr == null) return null;
        String[] parts = minStr.split(" ");
        if (parts.length < 3) return null;

        World world = Bukkit.getWorld(getWorldName());
        if (world == null) world = Bukkit.getWorlds().get(0);

        return new Location(world, Double.parseDouble(parts[0]), Double.parseDouble(parts[1]), Double.parseDouble(parts[2]));
    }

    public Location getRoomMax(String roomName) {
        String maxStr = config.getString("rooms." + roomName + ".max");
        if (maxStr == null) return null;
        String[] parts = maxStr.split(" ");
        if (parts.length < 3) return null;

        World world = Bukkit.getWorld(getWorldName());
        if (world == null) world = Bukkit.getWorlds().get(0);

        return new Location(world, Double.parseDouble(parts[0]), Double.parseDouble(parts[1]), Double.parseDouble(parts[2]));
    }

    public int getRoomMaxPlayersDefault(String roomName) { return config.getInt("rooms." + roomName + ".max-players-default", 4); }
    public String getRoomJoinPermission(String roomName) { return config.getString("rooms." + roomName + ".permissions.join", "all"); }
    public String getRoomMakePrivatePermission(String roomName) { return config.getString("rooms." + roomName + ".permissions.make-private", "all"); }

    public String getPrivateStateText(boolean isPrivate) {
        String path = "gui.strings.private-state." + (isPrivate ? "private" : "open") + ".text";
        return config.getString(path, isPrivate ? "Закрыта" : "Открыта");
    }

    public String getPrivateStateColor(boolean isPrivate) {
        String path = "gui.strings.private-state." + (isPrivate ? "private" : "open") + ".color";
        return config.getString(path, isPrivate ? "#FF0000" : "#00FF00");
    }

    public String getBusyStateText(boolean isBusy) {
        String path = "gui.strings.busy-state." + (isBusy ? "busy" : "free") + ".text";
        return config.getString(path, isBusy ? "Занята" : "Свободна");
    }

    public String getBusyStateColor(boolean isBusy) {
        String path = "gui.strings.busy-state." + (isBusy ? "busy" : "free") + ".color";
        return config.getString(path, isBusy ? "#FF0000" : "#00FF00");
    }

    public Set<String> getPageNames() {
        ConfigurationSection pagesSection = config.getConfigurationSection("gui.pages");
        return pagesSection != null ? pagesSection.getKeys(false) : new HashSet<>();
    }

    public String getPageName(String pageId) { return config.getString("gui.pages." + pageId + ".name", "PlayerRooms"); }
    public int getPageSize(String pageId) { return config.getInt("gui.pages." + pageId + ".size", 54); }
    public ConfigurationSection getPageSlots(String pageId) { return config.getConfigurationSection("gui.pages." + pageId + ".slots"); }
    public String getSlotName(String pageId, int slot) { return config.getString("gui.pages." + pageId + ".slots." + slot + ".name", ""); }
    public String getSlotMaterial(String pageId, int slot) { return config.getString("gui.pages." + pageId + ".slots." + slot + ".material", "BARRIER"); }
    public List<String> getSlotLore(String pageId, int slot) { return config.getStringList("gui.pages." + pageId + ".slots." + slot + ".lore"); }
    public String getSlotLinkedRoom(String pageId, int slot) { return config.getString("gui.pages." + pageId + ".slots." + slot + ".linked-room"); }
    public String getSlotAction(String pageId, int slot) { return config.getString("gui.pages." + pageId + ".slots." + slot + ".action"); }
}