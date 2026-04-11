package org.bestraxstudio.playerrooms.config;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Messages {

    private final JavaPlugin plugin;
    private YamlConfiguration messagesConfig;
    private final Map<String, String> messageCache;

    public Messages(JavaPlugin plugin) {
        this.plugin = plugin;
        this.messageCache = new HashMap<>();
        loadMessages();
    }

    public void loadMessages() {
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) plugin.saveResource("messages.yml", false);
        this.messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        messageCache.clear();
    }

    public String getMessage(String key) {
        if (messageCache.containsKey(key)) return messageCache.get(key);
        String message = messagesConfig.getString(key, "&cMessage not found: " + key);
        String colored = ChatColor.translateAlternateColorCodes('&', message);
        messageCache.put(key, colored);
        return colored;
    }

    public String getMessage(String key, Map<String, String> placeholders) {
        String message = getMessage(key);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return message;
    }

    public void reload() { loadMessages(); }
}