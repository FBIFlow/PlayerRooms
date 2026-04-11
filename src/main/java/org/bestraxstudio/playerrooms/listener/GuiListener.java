package org.bestraxstudio.playerrooms.listener;

import org.bestraxstudio.playerrooms.Loader;
import org.bestraxstudio.playerrooms.config.ConfigManager;
import org.bestraxstudio.playerrooms.config.Messages;
import org.bestraxstudio.playerrooms.gui.GuiBuilder;
import org.bestraxstudio.playerrooms.manager.PlayerRoomManager;
import org.bestraxstudio.playerrooms.model.Room;
import org.bestraxstudio.playerrooms.service.RoomService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class GuiListener implements Listener {

    private final Loader plugin;
    private final ConfigManager configManager;
    private final RoomService roomService;
    private final PlayerRoomManager playerRoomManager;
    private final GuiBuilder guiBuilder;
    private final Messages messages;

    public GuiListener(Loader plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.roomService = plugin.getRoomService();
        this.playerRoomManager = plugin.getPlayerRoomManager();
        this.guiBuilder = plugin.getGuiBuilder();
        this.messages = plugin.getMessages();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String currentPage = guiBuilder.getCurrentPage(player);
        if (currentPage == null) return;

        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType().isAir()) return;

        int slot = event.getSlot();
        String action = configManager.getSlotAction(currentPage, slot);
        if (action != null) {
            handleAction(player, action);
            return;
        }

        String linkedRoom = configManager.getSlotLinkedRoom(currentPage, slot);
        if (linkedRoom != null) handleRoomJoin(player, linkedRoom);
    }

    private void handleAction(Player player, String action) {
        switch (action.toLowerCase()) {
            case "next-page": nextPage(player); break;
            case "previous-page": previousPage(player); break;
        }
    }

    private void nextPage(Player player) {
        String currentPage = guiBuilder.getCurrentPage(player);
        Set<String> pages = guiBuilder.getAvailablePages();
        boolean found = false;
        String nextPage = null;
        for (String page : pages) {
            if (found) { nextPage = page; break; }
            if (page.equals(currentPage)) found = true;
        }
        if (nextPage != null) guiBuilder.openGui(player, nextPage);
    }

    private void previousPage(Player player) {
        String currentPage = guiBuilder.getCurrentPage(player);
        Set<String> pages = guiBuilder.getAvailablePages();
        String prevPage = null;
        String last = null;
        for (String page : pages) {
            if (page.equals(currentPage)) { prevPage = last; break; }
            last = page;
        }
        if (prevPage != null) guiBuilder.openGui(player, prevPage);
    }

    private void handleRoomJoin(Player player, String roomName) {
        Room room = roomService.getRoom(roomName);
        if (room == null) {
            player.sendMessage(messages.getMessage("join.room-not-found"));
            player.closeInventory();
            return;
        }

        Room currentRoom = roomService.getRoomByMember(player);
        if (currentRoom != null) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("room", currentRoom.getRoomName());
            player.sendMessage(messages.getMessage("join.already-in-room", placeholders));
            player.closeInventory();
            return;
        }

        if (!room.canJoin(player)) {
            if (room.isPrivate()) player.sendMessage(messages.getMessage("join.private"));
            else player.sendMessage(messages.getMessage("join.no-permission"));
            player.closeInventory();
            return;
        }

        if (room.isFull()) {
            if (player.hasPermission(configManager.getForceJoinPermission())) {
                player.sendMessage(messages.getMessage("join.force-join"));
            } else {
                player.sendMessage(messages.getMessage("join.full"));
                player.closeInventory();
                return;
            }
        }

        if (roomService.joinRoom(player, roomName)) {
            playerRoomManager.setPreviousLocation(player, player.getLocation());
            playerRoomManager.setPlayerRoom(player, roomName);
            player.teleport(room.getSpawnPoint());
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("room", room.getRoomName());
            player.sendMessage(messages.getMessage("join.success", placeholders));
            guiBuilder.refreshAllGuis();
            player.closeInventory();
        } else {
            player.sendMessage(messages.getMessage("join.error"));
        }
    }
}