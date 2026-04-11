package org.bestraxstudio.playerrooms.listener;

import org.bestraxstudio.playerrooms.Loader;
import org.bestraxstudio.playerrooms.model.Room;
import org.bestraxstudio.playerrooms.service.RoomService;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;

public class InteractionProtectionListener implements Listener {

    private final Loader plugin;
    private final RoomService roomService;

    public InteractionProtectionListener(Loader plugin) {
        this.plugin = plugin;
        this.roomService = plugin.getRoomService();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        Room room = roomService.getRoomByMember(player);
        if (room == null) return;
        if (event.getClickedBlock() != null) {
            event.setCancelled(true);
            player.sendMessage(plugin.getMessages().getMessage("protection.cannot-interact"));
        }
        if (event.getItem() != null && isDangerousItem(event.getItem().getType())) {
            event.setCancelled(true);
            player.sendMessage(plugin.getMessages().getMessage("protection.cannot-use"));
        }
    }

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        if (roomService.getRoomByMember(player) != null) {
            event.setCancelled(true);
            player.sendMessage(plugin.getMessages().getMessage("protection.cannot-use"));
        }
    }

    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent event) {
        Player player = event.getPlayer();
        if (roomService.getRoomByMember(player) != null) {
            event.setCancelled(true);
            player.sendMessage(plugin.getMessages().getMessage("protection.cannot-use"));
        }
    }

    @EventHandler
    public void onArmorStandManipulate(PlayerArmorStandManipulateEvent event) {
        Player player = event.getPlayer();
        if (roomService.getRoomByMember(player) != null) {
            event.setCancelled(true);
            player.sendMessage(plugin.getMessages().getMessage("protection.cannot-interact"));
        }
    }

    @EventHandler
    public void onHangingBreak(HangingBreakByEntityEvent event) {
        if (!(event.getRemover() instanceof Player)) return;
        Player player = (Player) event.getRemover();
        if (roomService.getRoomByMember(player) != null) {
            event.setCancelled(true);
            player.sendMessage(plugin.getMessages().getMessage("protection.cannot-break"));
        }
    }

    @EventHandler
    public void onHangingPlace(HangingPlaceEvent event) {
        Player player = event.getPlayer();
        if (roomService.getRoomByMember(player) != null) {
            event.setCancelled(true);
            player.sendMessage(plugin.getMessages().getMessage("protection.cannot-place"));
        }
    }

    private boolean isDangerousItem(Material material) {
        return material == Material.TNT || material == Material.FLINT_AND_STEEL ||
                material == Material.FIRE_CHARGE || material == Material.LAVA_BUCKET ||
                material == Material.WATER_BUCKET || material == Material.ENDER_PEARL ||
                material == Material.ENDER_EYE || material == Material.RESPAWN_ANCHOR ||
                material.name().contains("TNT");
    }
}