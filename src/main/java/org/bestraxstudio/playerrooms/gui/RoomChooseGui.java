package org.bestraxstudio.playerrooms.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class RoomChooseGui implements InventoryHolder {

    private final Inventory inventory;

    public RoomChooseGui() {
        inventory = Bukkit.createInventory(this, 54, Component.text("Room Choose"));
    }

    @Override
    public @NotNull Inventory getInventory() {
        return null;
    }
}
