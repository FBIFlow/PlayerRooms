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
                    player.sendMessage((messages.getCommandNoPermission()));
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
                player.sendMessage((messages.getCommandUnknown()));
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
            player.sendMessage((messages.getGuiNoPages()));
        }
    }

    private void leaveRoom(Player player) {
        Room currentRoom = roomService.getRoomByMember(player);
        if (currentRoom == null) {
            player.sendMessage((messages.getLeaveNotInRoom()));
            return;
        }

        Location previousLocation = playerRoomManager.getPreviousLocation(player);

        UUID newOwnerId = currentRoom.getNewOwnerAfterRemove(player);
        roomService.leaveCurrentRoom(player);

        if (newOwnerId != null) {
            Player newOwner = Bukkit.getPlayer(newOwnerId);
            if (newOwner != null && newOwner.isOnline()) {
                Map<String, Component> placeholders = new HashMap<>();
                placeholders.put("room", ComponentUtil.updateString( currentRoom.getRoomName()));
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

        Map<String, Component> placeholders = new HashMap<>();
        placeholders.put("room", ComponentUtil.updateString(currentRoom.getRoomName()) );
        player.sendMessage((messages.getLeaveSuccess(placeholders)));
    }

    private void reloadConfig(Player player) {
        plugin.getConfigManager().reload();
        plugin.getMessages().reload();
        roomService.loadRoomsFromConfig();
        plugin.getGuiBuilder().refreshAllGuis();
        plugin.reloadCommandBlocker();
        player.sendMessage((messages.getCommandReloadSuccess()));
    }

    private void showInfo(Player player) {
        Room currentRoom = roomService.getRoomByMember(player);
        if (currentRoom == null) {
            player.sendMessage((messages.getInfoNotInRoom()));
            return;
        }

        Map<String, Component> placeholders = new HashMap<>();

        placeholders.put("room", ComponentUtil.updateString(currentRoom.getRoomName()) );
        placeholders.put("current", ComponentUtil.updateString(String.valueOf(currentRoom.getCurrentPlayers())) );
        placeholders.put("max", ComponentUtil.updateString(String.valueOf(currentRoom.getMaxPlayers())) );
        player.sendMessage((messages.getInfoCurrent(placeholders)));
    }

    private void setPrivate(Player player, String[] args) {
        Room currentRoom = roomService.getRoomByMember(player);
        if (currentRoom == null) {
            player.sendMessage((messages.getSetprivateNotInRoom()));
            return;
        }

        if (!currentRoom.isOwner(player)) {
            player.sendMessage((messages.getSetprivateNotOwner()));
            return;
        }

        String requiredPerm = currentRoom.getMakePrivatePermission();
        if (!requiredPerm.equalsIgnoreCase("all") && !player.hasPermission(requiredPerm)) {
            player.sendMessage((messages.getSetprivateNoPermission()));
            return;
        }

        boolean newState;
        if (args.length >= 2) {
            if (args[1].equalsIgnoreCase("true") || args[1].equalsIgnoreCase("on")) {
                newState = true;
            } else if (args[1].equalsIgnoreCase("false") || args[1].equalsIgnoreCase("off")) {
                newState = false;
            } else {
                player.sendMessage((messages.getSetprivateInvalidArgument()));
                return;
            }
        } else {
            newState = !currentRoom.isPrivate();
        }

        roomService.setRoomPrivate(player, currentRoom.getRoomName(), newState);
        plugin.getGuiBuilder().refreshAllGuis();

        Map<String, Component> placeholders = new HashMap<>();
        placeholders.put("state", newState ? messages.getSetprivateClosedState() : messages.getSetprivateOpenedState());
        player.sendMessage((messages.getSetprivateSuccess(placeholders)));
    }

    private void invitePlayer(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage((messages.getInviteUsage()));
            return;
        }

        Room currentRoom = roomService.getRoomByMember(player);
        if (currentRoom == null) {
            player.sendMessage((messages.getInviteNotInRoom()));
            return;
        }

        if (!currentRoom.isOwner(player)) {
            player.sendMessage((messages.getInviteNotOwner()));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null || !target.isOnline()) {
            player.sendMessage((messages.getInvitePlayerNotFound()));
            return;
        }

        if (target.equals(player)) {
            player.sendMessage((messages.getInviteCannotInviteSelf()));
            return;
        }

        if (roomService.getRoomByMember(target) != null) {
            Map<String, Component> placeholders = new HashMap<>();
            placeholders.put("player", ComponentUtil.updateString(target.getName()));
            player.sendMessage((messages.getInviteAlreadyInRoom(placeholders)));
            return;
        }

        Map<String, Component> inviterPlaceholders = new HashMap<>();
        inviterPlaceholders.put("player", ComponentUtil.updateString(target.getName()));
        inviterPlaceholders.put("room", ComponentUtil.updateString(currentRoom.getRoomName()));
        player.sendMessage((messages.getInviteSent(inviterPlaceholders)));

        if (plugin.getIgnoreListManager().isIgnored(target, player)) {
            return;
        }

        invitationManager.createInvitation(player, target, currentRoom.getRoomName());

        Map<String, Component> targetPlaceholders = new HashMap<>();
        targetPlaceholders.put("player", ComponentUtil.updateString(player.getName()));
        targetPlaceholders.put("room", ComponentUtil.updateString(currentRoom.getRoomName()));
        Component inviteMessage = messages.getInviteReceived(targetPlaceholders);
        Component clickMessage = messages.getInviteClickToAccept(targetPlaceholders);
        target.sendMessage((clickMessage).clickEvent(ClickEvent.runCommand("room accept " + player.getName())));
        target.sendMessage((inviteMessage));
    }

    private void acceptInvitation(Player player, String[] args) {
        if (roomService.getRoomByMember(player) != null) {
            player.sendMessage((messages.getAcceptAlreadyInRoom()));
            return;
        }

        Invitation invitation;
        if (args.length >= 2) {
            invitation = invitationManager.getInvitationFrom(player, args[1]);
            if (invitation == null) {
                Map<String, Component> placeholders = new HashMap<>();
                placeholders.put("player", ComponentUtil.updateString(args[1]));
                player.sendMessage((messages.getAcceptInvitationNotFound(placeholders)));
                return;
            }
        } else {
            invitation = invitationManager.getLatestInvitation(player);
            if (invitation == null) {
                player.sendMessage((messages.getAcceptNoInvitations()));
                return;
            }
        }

        if (invitation.isExpired()) {
            invitationManager.removeInvitation(player, invitation);
            player.sendMessage((messages.getAcceptInvitationExpired()));
            return;
        }

        Player inviter = Bukkit.getPlayer(invitation.getInviter());
        if (inviter == null || !inviter.isOnline()) {
            player.sendMessage((messages.getAcceptInviterOffline()));
            invitationManager.removeInvitation(player, invitation);
            return;
        }

        Room room = roomService.getRoom(invitation.getRoomName());
        if (room == null) {
            player.sendMessage((messages.getAcceptRoomNotFound()));
            invitationManager.removeInvitation(player, invitation);
            return;
        }

        if (room.isFull() && !player.hasPermission(Loader.getInstance().getConfigManager().getForceJoinPermission())) {
            player.sendMessage((messages.getJoinFull()));
            invitationManager.removeInvitation(player, invitation);
            return;
        }

        if (roomService.joinRoomViaInvite(player, invitation.getRoomName())) {
            playerRoomManager.setPreviousLocation(player, player.getLocation());
            playerRoomManager.setPlayerRoom(player, invitation.getRoomName());
            player.teleport(room.getSpawnPoint());

            Map<String, Component> joinPlaceholders = new HashMap<>();
            joinPlaceholders.put("room", ComponentUtil.updateString(room.getRoomName()));
            player.sendMessage((messages.getJoinSuccess(joinPlaceholders)));

            Map<String, Component> inviterPlaceholders = new HashMap<>();
            inviterPlaceholders.put("player", ComponentUtil.updateString(player.getName()));
            inviterPlaceholders.put("room", ComponentUtil.updateString(room.getRoomName()));
            inviter.sendMessage(messages.getInvitePlayerJoined(inviterPlaceholders));

            plugin.getGuiBuilder().refreshAllGuis();
            invitationManager.removeInvitation(player, invitation);
        } else {
            player.sendMessage((messages.getJoinError()));
        }
    }

    private void ignorePlayer(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage((messages.getIgnoreUsage()));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null || !target.isOnline()) {
            player.sendMessage((messages.getIgnorePlayerNotFound()));
            return;
        }

        if (target.equals(player)) {
            player.sendMessage((messages.getIgnoreCannotIgnoreSelf()));
            return;
        }

        if (plugin.getIgnoreListManager().isIgnored(player, target)) {
            Map<String, Component> placeholders = new HashMap<>();
            placeholders.put("player", ComponentUtil.updateString(target.getName()));
            player.sendMessage((messages.getIgnoreAlreadyIgnored(placeholders)));
            return;
        }

        plugin.getIgnoreListManager().addIgnore(player, target);
        invitationManager.removeInvitationsFrom(player, target);

        Map<String, Component> placeholders = new HashMap<>();
        placeholders.put("player", ComponentUtil.updateString(target.getName()));
        player.sendMessage((messages.getIgnoreSuccess(placeholders)));
    }

    private void unignorePlayer(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage((messages.getUnignoreUsage()));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null || !target.isOnline()) {
            player.sendMessage((messages.getUnignorePlayerNotFound()));
            return;
        }

        if (!plugin.getIgnoreListManager().isIgnored(player, target)) {
            Map<String, Component> placeholders = new HashMap<>();
            placeholders.put("player", ComponentUtil.updateString(target.getName()));
            player.sendMessage((messages.getUnignoreNotIgnored(placeholders)));
            return;
        }

        plugin.getIgnoreListManager().removeIgnore(player, target);

        Map<String, Component> placeholders = new HashMap<>();
        placeholders.put("player", ComponentUtil.updateString(target.getName()));
        player.sendMessage((messages.getUnignoreSuccess(placeholders)));
    }

    private void showIgnoreList(Player player) {
        List<String> ignored = plugin.getIgnoreListManager().getIgnoreList(player);
        if (ignored.isEmpty()) {
            player.sendMessage((messages.getIgnoreListEmpty()));
            return;
        }

        player.sendMessage((messages.getIgnoreListHeader()));
        for (String name : ignored) {
            Map<String, Component> placeholders = new HashMap<>();
            placeholders.put("player", ComponentUtil.updateString(name));
            player.sendMessage((messages.getIgnoreListEntry(placeholders)));
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