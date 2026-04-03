package org.bestraxstudio.playerrooms.config;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class ConfigurationHolder {

    private final File configFile;
    private YamlConfiguration config;

    public ConfigurationHolder(File configFile) {
        String fileName = configFile.getName();
        if (!fileName.endsWith(".yml")) {
            configFile = new File(configFile.getParentFile(), fileName + ".yml");
        }
        this.configFile = configFile;
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
        }
    }

    public void loadConfig() {
        this.config = YamlConfiguration.loadConfiguration(configFile);
    }

    public void reload() {
        loadConfig();
    }

    public YamlConfiguration getYaml() {
        return config;
    }

}