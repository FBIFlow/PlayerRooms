package org.bestraxstudio.playerrooms.command;

import org.bestraxstudio.playerrooms.Loader;
import org.bestraxstudio.playerrooms.config.Messages;
import org.bestraxstudio.playerrooms.manager.InvitationManager;
import org.bestraxstudio.playerrooms.manager.PlayerRoomManager;
import org.bestraxstudio.playerrooms.model.Invitation;
import org.bestraxstudio.playerrooms.model.Room;
import org.bestraxstudio.playerrooms.service.RoomService;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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
    private final InvitationManager invitationManager;
    private final Messages messages;

    public RoomCommand(Loader plugin) {
        this.plugin = plugin;
        this.roomService = plugin.getRoomService();
        this.playerRoomManager = plugin.getPlayerRoomManager();
        this.invitationManager = plugin.getInvitationManager();
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
            case "invite":
                invitePlayer(player, args);
                break;
            case "accept":
                acceptInvitation(player, args);
                break;
            default:
                player.sendMessage(messages.getMessage("command.unknown"));
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

        Location previousLocation = playerRoomManager.getPreviousLocation(player);

        roomService.leaveCurrentRoom(player);
        playerRoomManager.removePlayer(player);
        plugin.getGuiBuilder().refreshAllGuis();

        if (previousLocation != null && previousLocation.getWorld() != null) {
            player.teleport(previousLocation);
        } else {
            Location spawn = player.getWorld().getSpawnLocation();
            player.teleport(spawn);
        }

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
                player.sendMessage(messages.getMessage("setprivate.invalid-argument"));
                return;
            }
        } else {
            newState = !currentRoom.isPrivate();
        }

        roomService.setRoomPrivate(player, currentRoom.getRoomName(), newState);
        plugin.getGuiBuilder().refreshAllGuis();

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("state", newState ? messages.getMessage("setprivate.closed-state") : messages.getMessage("setprivate.opened-state"));
        player.sendMessage(messages.getMessage("setprivate.success", placeholders));
    }

    private void invitePlayer(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(messages.getMessage("invite.usage"));
            return;
        }

        Room currentRoom = roomService.getRoomByMember(player);
        if (currentRoom == null) {
            player.sendMessage(messages.getMessage("invite.not-in-room"));
            return;
        }

        if (!currentRoom.isOwner(player)) {
            player.sendMessage(messages.getMessage("invite.not-owner"));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null || !target.isOnline()) {
            player.sendMessage(messages.getMessage("invite.player-not-found"));
            return;
        }

        if (target.equals(player)) {
            player.sendMessage(messages.getMessage("invite.cannot-invite-self"));
            return;
        }

        if (roomService.getRoomByMember(target) != null) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("player", target.getName());
            player.sendMessage(messages.getMessage("invite.already-in-room", placeholders));
            return;
        }

        invitationManager.createInvitation(player, target, currentRoom.getRoomName());

        Map<String, String> inviterPlaceholders = new HashMap<>();
        inviterPlaceholders.put("player", target.getName());
        inviterPlaceholders.put("room", currentRoom.getRoomName());
        player.sendMessage(messages.getMessage("invite.sent", inviterPlaceholders));

        Map<String, String> targetPlaceholders = new HashMap<>();
        targetPlaceholders.put("player", player.getName());
        targetPlaceholders.put("room", currentRoom.getRoomName());
        String inviteMessage = messages.getMessage("invite.received", targetPlaceholders);
        String clickMessage = messages.getMessage("invite.click-to-accept", targetPlaceholders);
        target.sendMessage(inviteMessage);
        target.sendMessage(clickMessage);
    }

    private void acceptInvitation(Player player, String[] args) {
        if (roomService.getRoomByMember(player) != null) {
            player.sendMessage(messages.getMessage("accept.already-in-room"));
            return;
        }

        Invitation invitation;
        if (args.length >= 2) {
            invitation = invitationManager.getInvitationFrom(player, args[1]);
            if (invitation == null) {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("player", args[1]);
                player.sendMessage(messages.getMessage("accept.invitation-not-found", placeholders));
                return;
            }
        } else {
            invitation = invitationManager.getLatestInvitation(player);
            if (invitation == null) {
                player.sendMessage(messages.getMessage("accept.no-invitations"));
                return;
            }
        }

        if (invitation.isExpired()) {
            invitationManager.removeInvitation(player, invitation);
            player.sendMessage(messages.getMessage("accept.invitation-expired"));
            return;
        }

        Player inviter = Bukkit.getPlayer(invitation.getInviter());
        if (inviter == null || !inviter.isOnline()) {
            player.sendMessage(messages.getMessage("accept.inviter-offline"));
            invitationManager.removeInvitation(player, invitation);
            return;
        }

        Room room = roomService.getRoom(invitation.getRoomName());
        if (room == null) {
            player.sendMessage(messages.getMessage("accept.room-not-found"));
            invitationManager.removeInvitation(player, invitation);
            return;
        }

        if (room.isFull()) {
            player.sendMessage(messages.getMessage("join.full"));
            invitationManager.removeInvitation(player, invitation);
            return;
        }

        if (roomService.joinRoomViaInvite(player, invitation.getRoomName())) {
            playerRoomManager.setPreviousLocation(player, player.getLocation());
            playerRoomManager.setPlayerRoom(player, invitation.getRoomName());
            player.teleport(room.getSpawnPoint());

            Map<String, String> joinPlaceholders = new HashMap<>();
            joinPlaceholders.put("room", room.getRoomName());
            player.sendMessage(messages.getMessage("join.success", joinPlaceholders));

            Map<String, String> inviterPlaceholders = new HashMap<>();
            inviterPlaceholders.put("player", player.getName());
            inviterPlaceholders.put("room", room.getRoomName());
            inviter.sendMessage(messages.getMessage("invite.player-joined", inviterPlaceholders));

            plugin.getGuiBuilder().refreshAllGuis();
            invitationManager.removeInvitation(player, invitation);
        } else {
            player.sendMessage(messages.getMessage("join.error"));
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) return List.of();

        Player player = (Player) sender;
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("leave");
            completions.add("info");
            completions.add("accept");
            if (roomService.getRoomByMember(player) != null && roomService.getRoomByMember(player).isOwner(player)) {
                completions.add("setprivate");
                completions.add("invite");
            }
            if (player.hasPermission(plugin.getConfigManager().getAdminPermission())) {
                completions.add("reload");
            }
            return completions;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("setprivate")) {
            Room room = roomService.getRoomByMember(player);
            if (room != null && room.isOwner(player)) {
                completions.add("true");
                completions.add("false");
            }
            return completions;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("invite")) {
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (!online.equals(player) && roomService.getRoomByMember(online) == null) {
                    completions.add(online.getName());
                }
            }
            return completions;
        }

        return List.of();
    }
}