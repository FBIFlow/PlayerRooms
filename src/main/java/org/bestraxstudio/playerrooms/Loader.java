package org.bestraxstudio.playerrooms;

import org.bestraxstudio.playerrooms.command.RoomCommand;
import org.bestraxstudio.playerrooms.config.ConfigurationHolder;
import org.bestraxstudio.playerrooms.gui.RoomChooseGui;
import org.bestraxstudio.playerrooms.listener.GuiListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class Loader extends JavaPlugin {

    private static Loader instance;

    private ConfigurationHolder config;

    private RoomChooseGui roomChooseGui;
    private RoomCommand roomCommand;
    private GuiListener guiListener;

    @Override
    public void onEnable() {
        instance = this;

        this.config = new ConfigurationHolder(new File(getDataFolder(), "config.yml"));

        this.roomChooseGui = new RoomChooseGui(config.getYaml().getString("gui.name"));
        this.roomCommand = new RoomCommand();
        this.guiListener = new GuiListener();

        var bukkitCommand = getServer().getPluginCommand("room");
        if (bukkitCommand == null) {
            throw new RuntimeException("No command found.");
        }
        bukkitCommand.setExecutor(roomCommand);
        bukkitCommand.setTabCompleter(roomCommand);

        getServer().getPluginManager().registerEvents(guiListener, this);
    }

    @Override
    public void onDisable() {

    }

    public static Loader getInstance() {
        return instance;
    }

    public RoomChooseGui getRoomChooseGui() {
        return roomChooseGui;
    }

}