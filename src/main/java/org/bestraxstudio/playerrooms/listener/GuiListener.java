package org.bestraxstudio.playerrooms.listener;

import org.bestraxstudio.playerrooms.Loader;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class GuiListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(Loader.getInstance().getRoomChooseGui().getInventory())) {
            return;
        }
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null) {
            return;
        }
        event.setCancelled(true);
    }

}