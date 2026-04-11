package org.bestraxstudio.playerrooms;

import org.bestraxstudio.playerrooms.command.RoomCommand;
import org.bestraxstudio.playerrooms.config.ConfigManager;
import org.bestraxstudio.playerrooms.config.Messages;
import org.bestraxstudio.playerrooms.gui.GuiBuilder;
import org.bestraxstudio.playerrooms.listener.*;
import org.bestraxstudio.playerrooms.manager.InvitationManager;
import org.bestraxstudio.playerrooms.manager.PlayerRoomManager;
import org.bestraxstudio.playerrooms.service.RoomService;
import org.bukkit.plugin.java.JavaPlugin;

public class Loader extends JavaPlugin {

    private static Loader instance;
    private ConfigManager configManager;
    private Messages messages;
    private RoomService roomService;
    private PlayerRoomManager playerRoomManager;
    private InvitationManager invitationManager;
    private GuiBuilder guiBuilder;

    @Override
    public void onEnable() {
        instance = this;

        this.configManager = new ConfigManager(this);
        this.messages = new Messages(this);
        this.roomService = new RoomService(this);
        this.playerRoomManager = new PlayerRoomManager();
        this.invitationManager = new InvitationManager();
        this.guiBuilder = new GuiBuilder(this);

        this.roomService.loadRoomsFromConfig();

        RoomCommand command = new RoomCommand(this);
        java.util.Objects.requireNonNull(getServer().getPluginCommand("room")).setExecutor(command);
        getServer().getPluginCommand("room").setTabCompleter(command);

        getServer().getPluginManager().registerEvents(new GuiListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new RoomProtectionListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockProtectionListener(this), this);
        getServer().getPluginManager().registerEvents(new InteractionProtectionListener(this), this);
        getServer().getPluginManager().registerEvents(new PVPProtectionListener(this), this);

        if (configManager.getGenerateTestRooms()) {
            TestRoomGenerator generator = new TestRoomGenerator(this);
            generator.generateAllRooms();
        }

        getLogger().info(messages.getMessage("plugin.enabled",
                java.util.Map.of("count", String.valueOf(roomService.getAllRooms().size()))));
    }

    @Override
    public void onDisable() {
        if (playerRoomManager != null) playerRoomManager.clearAll();
        if (invitationManager != null) invitationManager.cleanup();
        getLogger().info(messages.getMessage("plugin.disabled"));
    }

    public static Loader getInstance() { return instance; }
    public ConfigManager getConfigManager() { return configManager; }
    public Messages getMessages() { return messages; }
    public RoomService getRoomService() { return roomService; }
    public PlayerRoomManager getPlayerRoomManager() { return playerRoomManager; }
    public InvitationManager getInvitationManager() { return invitationManager; }
    public GuiBuilder getGuiBuilder() { return guiBuilder; }
}