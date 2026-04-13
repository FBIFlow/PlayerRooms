package org.bestraxstudio.playerrooms.listener;

import org.bestraxstudio.playerrooms.Loader;
import org.bestraxstudio.playerrooms.config.Messages;
import org.bestraxstudio.playerrooms.gui.GuiBuilder;
import org.bestraxstudio.playerrooms.manager.PlayerRoomManager;
import org.bestraxstudio.playerrooms.model.Room;
import org.bestraxstudio.playerrooms.service.RoomService;
import org.bukkit.Location;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.projectiles.ProjectileSource;

import java.util.HashMap;
import java.util.Map;

public class RoomProtectionListener implements Listener {

    private final Loader plugin;
    private final RoomService roomService;
    private final PlayerRoomManager playerRoomManager;
    private final Messages messages;
    private final GuiBuilder guiBuilder;

    public RoomProtectionListener(Loader plugin) {
        this.plugin = plugin;
        this.roomService = plugin.getRoomService();
        this.playerRoomManager = plugin.getPlayerRoomManager();
        this.messages = plugin.getMessages();
        this.guiBuilder = plugin.getGuiBuilder();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        Room currentRoom = roomService.getRoomByMember(player);
        if (currentRoom == null || !currentRoom.hasBounds()) return;
        Location to = event.getTo();
        if (to == null) return;
        if (!currentRoom.isInsideBounds(to)) {
            handlePlayerLeaveRoom(player, currentRoom);
            event.setCancelled(true);
            player.teleport(player.getWorld().getSpawnLocation());
            player.teleport(currentRoom.getSpawnPoint());
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        Room currentRoom = roomService.getRoomByMember(player);
        if (currentRoom == null) return;
        Location to = event.getTo();
        if (currentRoom.hasBounds() && !currentRoom.isInsideBounds(to)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEnderPearlHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof EnderPearl)) return;
        EnderPearl pearl = (EnderPearl) event.getEntity();
        ProjectileSource shooter = pearl.getShooter();
        if (!(shooter instanceof Player)) return;
        Player player = (Player) shooter;
        Room currentRoom = roomService.getRoomByMember(player);
        if (currentRoom == null || !currentRoom.hasBounds()) return;
        if (!currentRoom.isInsideBounds(pearl.getLocation())) {
            pearl.remove();
        }
    }

    private void handlePlayerLeaveRoom(Player player, Room room) {
        roomService.leaveCurrentRoom(player);
        playerRoomManager.removePlayer(player);
        guiBuilder.refreshAllGuis();
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("room", room.getRoomName());
        player.sendMessage(messages.getLeaveBoundsExit(placeholders));
    }
}