package org.bestraxstudio.playerrooms.listener;

import org.bestraxstudio.playerrooms.Loader;
import org.bestraxstudio.playerrooms.model.Room;
import org.bestraxstudio.playerrooms.service.RoomService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class PVPProtectionListener implements Listener {

    private final Loader plugin;
    private final RoomService roomService;

    public PVPProtectionListener(Loader plugin) {
        this.plugin = plugin;
        this.roomService = plugin.getRoomService();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getDamager() instanceof Player)) return;
        if (!(event.getEntity() instanceof Player)) return;

        Player attacker = (Player) event.getDamager();
        Player victim = (Player) event.getEntity();
        Room attackerRoom = roomService.getRoomByMember(attacker);
        Room victimRoom = roomService.getRoomByMember(victim);

        if (attackerRoom != null && attackerRoom.equals(victimRoom)) {
            event.setCancelled(true);
            attacker.sendMessage(plugin.getMessages().getProtectionPvpDisabled());
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        Room room = roomService.getRoomByMember(player);
        if (room != null && plugin.getConfigManager().isDisableFallDamage()) {
            EntityDamageEvent.DamageCause cause = event.getCause();
            if (cause == EntityDamageEvent.DamageCause.FALL ||
                    cause == EntityDamageEvent.DamageCause.FIRE ||
                    cause == EntityDamageEvent.DamageCause.FIRE_TICK ||
                    cause == EntityDamageEvent.DamageCause.LAVA ||
                    cause == EntityDamageEvent.DamageCause.DROWNING) {
                event.setCancelled(true);
            }
        }
    }
}