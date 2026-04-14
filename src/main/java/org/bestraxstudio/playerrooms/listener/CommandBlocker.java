package org.bestraxstudio.playerrooms.listener;

import org.bestraxstudio.playerrooms.Loader;
import org.bestraxstudio.playerrooms.model.Room;
import org.bestraxstudio.playerrooms.service.RoomService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.List;

public class CommandBlocker implements Listener {

    private final Loader plugin;
    private final RoomService roomService;
    private List<String> allowedCommands;

    public CommandBlocker(Loader plugin) {
        this.plugin = plugin;
        this.roomService = plugin.getRoomService();
        this.allowedCommands = plugin.getConfigManager().getAllowedCommands();
        this.allowedCommands.add("room");
    }

    public void reloadAllowedCommands() {
        this.allowedCommands = plugin.getConfigManager().getAllowedCommands();
        this.allowedCommands.add("room");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        Room currentRoom = roomService.getRoomByMember(player);
        if (currentRoom == null) return;

        String command = event.getMessage().toLowerCase();
        if (command.startsWith("/")) {
            command = command.substring(1);
        }
        String[] parts = command.split(" ");
        String baseCommand = parts[0];

        if (allowedCommands.contains(baseCommand)) {
            return;
        }

        event.setCancelled(true);
        player.sendMessage(plugin.getMessages().getCommandBlocked());
    }
}