package org.bestraxstudio.playerrooms.listener;

import org.bestraxstudio.playerrooms.Loader;
import org.bestraxstudio.playerrooms.model.Room;
import org.bestraxstudio.playerrooms.service.RoomService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;

public class BlockProtectionListener implements Listener {

    private final Loader plugin;
    private final RoomService roomService;

    public BlockProtectionListener(Loader plugin) {
        this.plugin = plugin;
        this.roomService = plugin.getRoomService();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        if (roomService.getRoomByMember(player) != null) {
            event.setCancelled(true);
            player.sendMessage(plugin.getMessages().getProtectionCannotBreak());
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        if (roomService.getRoomByMember(player) != null) {
            event.setCancelled(true);
            player.sendMessage(plugin.getMessages().getProtectionCannotPlace());
        }
    }

    @EventHandler
    public void onBlockFromTo(BlockFromToEvent event) {
        for (Room room : roomService.getAllRooms().values()) {
            if (room.hasBounds() && room.isInsideBounds(event.getToBlock().getLocation())) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {
        for (Room room : roomService.getAllRooms().values()) {
            if (room.hasBounds() && room.isInsideBounds(event.getBlock().getLocation())) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent event) {
        for (Room room : roomService.getAllRooms().values()) {
            if (!room.hasBounds()) continue;
            for (int i = 0; i <= event.getLength(); i++) {
                var movedBlock = event.getBlock().getRelative(event.getDirection(), i + 1);
                if (room.isInsideBounds(movedBlock.getLocation())) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent event) {
        if (!event.isSticky()) return;
        var movedBlock = event.getBlock().getRelative(event.getDirection(), 2);
        for (Room room : roomService.getAllRooms().values()) {
            if (room.hasBounds() && room.isInsideBounds(movedBlock.getLocation())) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent event) {
        for (Room room : roomService.getAllRooms().values()) {
            if (room.hasBounds() && room.isInsideBounds(event.getBlock().getLocation())) {
                event.setCancelled(true);
                return;
            }
        }
    }
}