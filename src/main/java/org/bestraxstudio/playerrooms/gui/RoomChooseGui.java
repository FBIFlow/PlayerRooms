package org.bestraxstudio.playerrooms.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class RoomChooseGui implements InventoryHolder {

    private String title;
    private Inventory inventory;

    public RoomChooseGui(String title) {
        this.title = title;
        inventory = Bukkit.createInventory(this, 54, Component.text(title));
    }

    public String getTitle() {
        return this.title;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
