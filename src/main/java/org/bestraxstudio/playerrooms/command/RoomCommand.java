package org.bestraxstudio.playerrooms.command;

import org.bestraxstudio.playerrooms.Loader;
import org.bestraxstudio.playerrooms.config.Messages;
import org.bestraxstudio.playerrooms.manager.PlayerRoomManager;
import org.bestraxstudio.playerrooms.model.Room;
import org.bestraxstudio.playerrooms.service.RoomService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class RoomCommand implements CommandExecutor, TabCompleter {

    private final Loader plugin;
    private final RoomService roomService;
    private final PlayerRoomManager playerRoomManager;
    private final Messages messages;

    public RoomCommand(Loader plugin) {
        this.plugin = plugin;
        this.roomService = plugin.getRoomService();
        this.playerRoomManager = plugin.getPlayerRoomManager();
        this.messages = plugin.getMessages();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(messages.getMessage("command.only-players"));
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            openGui(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "leave":
                leaveRoom(player);
                break;
            case "reload":
                if (player.hasPermission(plugin.getConfigManager().getAdminPermission())) {
                    reloadConfig(player);
                } else {
                    player.sendMessage(messages.getMessage("command.no-permission"));
                }
                break;
            case "info":
                showInfo(player);
                break;
            case "setprivate":
                setPrivate(player, args);
                break;
            default:
                openGui(player);
                break;
        }
        return true;
    }

    private void openGui(Player player) {
        Set<String> pages = plugin.getGuiBuilder().getAvailablePages();
        String firstPage = pages.isEmpty() ? null : pages.iterator().next();
        if (firstPage != null) {
            plugin.getGuiBuilder().openGui(player, firstPage);
        } else {
            player.sendMessage(messages.getMessage("gui.no-pages"));
        }
    }

    private void leaveRoom(Player player) {
        Room currentRoom = roomService.getRoomByMember(player);
        if (currentRoom == null) {
            player.sendMessage(messages.getMessage("leave.not-in-room"));
            return;
        }

        roomService.leaveCurrentRoom(player);
        playerRoomManager.removePlayer(player);
        plugin.getGuiBuilder().refreshAllGuis();

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("room", currentRoom.getRoomName());
        player.sendMessage(messages.getMessage("leave.success", placeholders));
    }

    private void reloadConfig(Player player) {
        plugin.getConfigManager().reload();
        plugin.getMessages().reload();
        roomService.loadRoomsFromConfig();
        plugin.getGuiBuilder().refreshAllGuis();
        player.sendMessage(messages.getMessage("command.reload-success"));
    }

    private void showInfo(Player player) {
        Room currentRoom = roomService.getRoomByMember(player);
        if (currentRoom == null) {
            player.sendMessage(messages.getMessage("info.not-in-room"));
            return;
        }

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("room", currentRoom.getRoomName());
        placeholders.put("current", String.valueOf(currentRoom.getCurrentPlayers()));
        placeholders.put("max", String.valueOf(currentRoom.getMaxPlayers()));
        player.sendMessage(messages.getMessage("info.current", placeholders));
    }

    private void setPrivate(Player player, String[] args) {
        Room currentRoom = roomService.getRoomByMember(player);
        if (currentRoom == null) {
            player.sendMessage(messages.getMessage("setprivate.not-in-room"));
            return;
        }

        if (!currentRoom.isOwner(player)) {
            player.sendMessage(messages.getMessage("setprivate.not-owner"));
            return;
        }

        String requiredPerm = currentRoom.getMakePrivatePermission();
        if (!requiredPerm.equalsIgnoreCase("all") && !player.hasPermission(requiredPerm)) {
            player.sendMessage(messages.getMessage("setprivate.no-permission"));
            return;
        }

        boolean newState;
        if (args.length >= 2) {
            if (args[1].equalsIgnoreCase("true") || args[1].equalsIgnoreCase("on")) {
                newState = true;
            } else if (args[1].equalsIgnoreCase("false") || args[1].equalsIgnoreCase("off")) {
                newState = false;
            } else {
                newState = !currentRoom.isPrivate();
            }
        } else {
            newState = !currentRoom.isPrivate();
        }

        roomService.setRoomPrivate(player, currentRoom.getRoomName(), newState);
        plugin.getGuiBuilder().refreshAllGuis();

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("state", newState ? "закрыта" : "открыта");
        player.sendMessage(messages.getMessage("setprivate.success", placeholders));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) return List.of();

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("leave");
            completions.add("info");
            completions.add("setprivate");
            if (sender.hasPermission(plugin.getConfigManager().getAdminPermission())) {
                completions.add("reload");
            }
            return completions;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("setprivate")) {
            completions.add("true");
            completions.add("false");
            return completions;
        }

        return List.of();
    }
}