package org.bestraxstudio.playerrooms.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bestraxstudio.playerrooms.Loader;
import org.bestraxstudio.playerrooms.config.Messages;
import org.bestraxstudio.playerrooms.manager.InvitationManager;
import org.bestraxstudio.playerrooms.manager.PlayerRoomManager;
import org.bestraxstudio.playerrooms.model.Invitation;
import org.bestraxstudio.playerrooms.model.Room;
import org.bestraxstudio.playerrooms.service.RoomService;
import org.bestraxstudio.playerrooms.util.ComponentUtil;
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
            sender.sendMessage(messages.getCommandOnlyPlayers());
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
                    player.sendMessage(ComponentUtil.updateString(messages.getCommandNoPermission()));
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
            case "ignore":
                ignorePlayer(player, args);
                break;
            case "unignore":
                unignorePlayer(player, args);
                break;
            case "ignorelist":
                showIgnoreList(player);
                break;
            default:
                player.sendMessage(ComponentUtil.updateString(messages.getCommandUnknown()));
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
            player.sendMessage(ComponentUtil.updateString(messages.getGuiNoPages()));
        }
    }

    private void leaveRoom(Player player) {
        Room currentRoom = roomService.getRoomByMember(player);
        if (currentRoom == null) {
            player.sendMessage(ComponentUtil.updateString(messages.getLeaveNotInRoom()));
            return;
        }

        Location previousLocation = playerRoomManager.getPreviousLocation(player);

        UUID newOwnerId = currentRoom.getNewOwnerAfterRemove(player);
        roomService.leaveCurrentRoom(player);

        if (newOwnerId != null) {
            Player newOwner = Bukkit.getPlayer(newOwnerId);
            if (newOwner != null && newOwner.isOnline()) {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("room", currentRoom.getRoomName());
                newOwner.sendMessage(messages.getOwnerTransfer(placeholders));
            }
        }

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
        player.sendMessage(ComponentUtil.updateString(messages.getLeaveSuccess(placeholders)));
    }

    private void reloadConfig(Player player) {
        plugin.getConfigManager().reload();
        plugin.getMessages().reload();
        roomService.loadRoomsFromConfig();
        plugin.getGuiBuilder().refreshAllGuis();
        plugin.reloadCommandBlocker();
        player.sendMessage(ComponentUtil.updateString(messages.getCommandReloadSuccess()));
    }

    private void showInfo(Player player) {
        Room currentRoom = roomService.getRoomByMember(player);
        if (currentRoom == null) {
            player.sendMessage(ComponentUtil.updateString(messages.getInfoNotInRoom()));
            return;
        }

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("room", currentRoom.getRoomName());
        placeholders.put("current", String.valueOf(currentRoom.getCurrentPlayers()));
        placeholders.put("max", String.valueOf(currentRoom.getMaxPlayers()));
        player.sendMessage(ComponentUtil.updateString(messages.getInfoCurrent(placeholders)));
    }

    private void setPrivate(Player player, String[] args) {
        Room currentRoom = roomService.getRoomByMember(player);
        if (currentRoom == null) {
            player.sendMessage(ComponentUtil.updateString(messages.getSetprivateNotInRoom()));
            return;
        }

        if (!currentRoom.isOwner(player)) {
            player.sendMessage(ComponentUtil.updateString(messages.getSetprivateNotOwner()));
            return;
        }

        String requiredPerm = currentRoom.getMakePrivatePermission();
        if (!requiredPerm.equalsIgnoreCase("all") && !player.hasPermission(requiredPerm)) {
            player.sendMessage(ComponentUtil.updateString(messages.getSetprivateNoPermission()));
            return;
        }

        boolean newState;
        if (args.length >= 2) {
            if (args[1].equalsIgnoreCase("true") || args[1].equalsIgnoreCase("on")) {
                newState = true;
            } else if (args[1].equalsIgnoreCase("false") || args[1].equalsIgnoreCase("off")) {
                newState = false;
            } else {
                player.sendMessage(ComponentUtil.updateString(messages.getSetprivateInvalidArgument()));
                return;
            }
        } else {
            newState = !currentRoom.isPrivate();
        }

        roomService.setRoomPrivate(player, currentRoom.getRoomName(), newState);
        plugin.getGuiBuilder().refreshAllGuis();

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("state", newState ? messages.getSetprivateClosedState() : messages.getSetprivateOpenedState());
        player.sendMessage(ComponentUtil.updateString(messages.getSetprivateSuccess(placeholders)));
    }

    private void invitePlayer(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ComponentUtil.updateString(messages.getInviteUsage()));
            return;
        }

        Room currentRoom = roomService.getRoomByMember(player);
        if (currentRoom == null) {
            player.sendMessage(ComponentUtil.updateString(messages.getInviteNotInRoom()));
            return;
        }

        if (!currentRoom.isOwner(player)) {
            player.sendMessage(ComponentUtil.updateString(messages.getInviteNotOwner()));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null || !target.isOnline()) {
            player.sendMessage(ComponentUtil.updateString(messages.getInvitePlayerNotFound()));
            return;
        }

        if (target.equals(player)) {
            player.sendMessage(ComponentUtil.updateString(messages.getInviteCannotInviteSelf()));
            return;
        }

        if (roomService.getRoomByMember(target) != null) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("player", target.getName());
            player.sendMessage(ComponentUtil.updateString(messages.getInviteAlreadyInRoom(placeholders)));
            return;
        }

        Map<String, String> inviterPlaceholders = new HashMap<>();
        inviterPlaceholders.put("player", target.getName());
        inviterPlaceholders.put("room", currentRoom.getRoomName());
        player.sendMessage(ComponentUtil.updateString(messages.getInviteSent(inviterPlaceholders)));

        if (plugin.getIgnoreListManager().isIgnored(target, player)) {
            return;
        }

        invitationManager.createInvitation(player, target, currentRoom.getRoomName());

        Map<String, String> targetPlaceholders = new HashMap<>();
        targetPlaceholders.put("player", player.getName());
        targetPlaceholders.put("room", currentRoom.getRoomName());
        String inviteMessage = messages.getInviteReceived(targetPlaceholders);
        String clickMessage = messages.getInviteClickToAccept(targetPlaceholders);
        target.sendMessage(ComponentUtil.updateString(clickMessage).clickEvent(ClickEvent.runCommand("room accept " + player.getName())));
        target.sendMessage(ComponentUtil.updateString(inviteMessage));
    }

    private void acceptInvitation(Player player, String[] args) {
        if (roomService.getRoomByMember(player) != null) {
            player.sendMessage(ComponentUtil.updateString(messages.getAcceptAlreadyInRoom()));
            return;
        }

        Invitation invitation;
        if (args.length >= 2) {
            invitation = invitationManager.getInvitationFrom(player, args[1]);
            if (invitation == null) {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("player", args[1]);
                player.sendMessage(ComponentUtil.updateString(messages.getAcceptInvitationNotFound(placeholders)));
                return;
            }
        } else {
            invitation = invitationManager.getLatestInvitation(player);
            if (invitation == null) {
                player.sendMessage(ComponentUtil.updateString(messages.getAcceptNoInvitations()));
                return;
            }
        }

        if (invitation.isExpired()) {
            invitationManager.removeInvitation(player, invitation);
            player.sendMessage(ComponentUtil.updateString(messages.getAcceptInvitationExpired()));
            return;
        }

        Player inviter = Bukkit.getPlayer(invitation.getInviter());
        if (inviter == null || !inviter.isOnline()) {
            player.sendMessage(ComponentUtil.updateString(messages.getAcceptInviterOffline()));
            invitationManager.removeInvitation(player, invitation);
            return;
        }

        Room room = roomService.getRoom(invitation.getRoomName());
        if (room == null) {
            player.sendMessage(ComponentUtil.updateString(messages.getAcceptRoomNotFound()));
            invitationManager.removeInvitation(player, invitation);
            return;
        }

        if (room.isFull() && !player.hasPermission(Loader.getInstance().getConfigManager().getForceJoinPermission())) {
            player.sendMessage(ComponentUtil.updateString(messages.getJoinFull()));
            invitationManager.removeInvitation(player, invitation);
            return;
        }

        if (roomService.joinRoomViaInvite(player, invitation.getRoomName())) {
            playerRoomManager.setPreviousLocation(player, player.getLocation());
            playerRoomManager.setPlayerRoom(player, invitation.getRoomName());
            player.teleport(room.getSpawnPoint());

            Map<String, String> joinPlaceholders = new HashMap<>();
            joinPlaceholders.put("room", room.getRoomName());
            player.sendMessage(ComponentUtil.updateString(messages.getJoinSuccess(joinPlaceholders)));

            Map<String, String> inviterPlaceholders = new HashMap<>();
            inviterPlaceholders.put("player", player.getName());
            inviterPlaceholders.put("room", room.getRoomName());
            inviter.sendMessage(messages.getInvitePlayerJoined(inviterPlaceholders));

            plugin.getGuiBuilder().refreshAllGuis();
            invitationManager.removeInvitation(player, invitation);
        } else {
            player.sendMessage(ComponentUtil.updateString(messages.getJoinError()));
        }
    }

    private void ignorePlayer(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ComponentUtil.updateString(messages.getIgnoreUsage()));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null || !target.isOnline()) {
            player.sendMessage(ComponentUtil.updateString(messages.getIgnorePlayerNotFound()));
            return;
        }

        if (target.equals(player)) {
            player.sendMessage(ComponentUtil.updateString(messages.getIgnoreCannotIgnoreSelf()));
            return;
        }

        if (plugin.getIgnoreListManager().isIgnored(player, target)) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("player", target.getName());
            player.sendMessage(ComponentUtil.updateString(messages.getIgnoreAlreadyIgnored(placeholders)));
            return;
        }

        plugin.getIgnoreListManager().addIgnore(player, target);
        invitationManager.removeInvitationsFrom(player, target);

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", target.getName());
        player.sendMessage(ComponentUtil.updateString(messages.getIgnoreSuccess(placeholders)));
    }

    private void unignorePlayer(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ComponentUtil.updateString(messages.getUnignoreUsage()));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null || !target.isOnline()) {
            player.sendMessage(ComponentUtil.updateString(messages.getUnignorePlayerNotFound()));
            return;
        }

        if (!plugin.getIgnoreListManager().isIgnored(player, target)) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("player", target.getName());
            player.sendMessage(ComponentUtil.updateString(messages.getUnignoreNotIgnored(placeholders)));
            return;
        }

        plugin.getIgnoreListManager().removeIgnore(player, target);

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", target.getName());
        player.sendMessage(ComponentUtil.updateString(messages.getUnignoreSuccess(placeholders)));
    }

    private void showIgnoreList(Player player) {
        List<String> ignored = plugin.getIgnoreListManager().getIgnoreList(player);
        if (ignored.isEmpty()) {
            player.sendMessage(ComponentUtil.updateString(messages.getIgnoreListEmpty()));
            return;
        }

        player.sendMessage(ComponentUtil.updateString(messages.getIgnoreListHeader()));
        for (String name : ignored) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("player", name);
            player.sendMessage(ComponentUtil.updateString(messages.getIgnoreListEntry(placeholders)));
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
            completions.add("ignore");
            completions.add("unignore");
            completions.add("ignorelist");
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

        if (args.length == 2 && (args[0].equalsIgnoreCase("invite") || args[0].equalsIgnoreCase("ignore") || args[0].equalsIgnoreCase("unignore"))) {
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (!online.equals(player)) {
                    completions.add(online.getName());
                }
            }
            return completions;
        }

        return List.of();
    }
}