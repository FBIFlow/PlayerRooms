package org.bestraxstudio.playerrooms.listener;

import org.bestraxstudio.playerrooms.Loader;
import org.bestraxstudio.playerrooms.config.ConfigManager;
import org.bestraxstudio.playerrooms.config.Messages;
import org.bestraxstudio.playerrooms.gui.GuiBuilder;
import org.bestraxstudio.playerrooms.manager.PlayerRoomManager;
import org.bestraxstudio.playerrooms.model.Room;
import org.bestraxstudio.playerrooms.service.RoomService;
import org.bestraxstudio.playerrooms.util.ComponentUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

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

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        String currentPage = guiBuilder.getCurrentPage(player);
        if (currentPage == null) return;

        if (event.getClickedInventory() == null) return;
        if (event.getClickedInventory() != event.getView().getTopInventory()) {
            event.setCancelled(true);
            return;
        }

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
            case "next-page":
                nextPage(player);
                break;
            case "previous-page":
                previousPage(player);
                break;
            case "leave":
                leaveRoom(player);
                break;
        }
    }

    private void nextPage(Player player) {
        String currentPage = guiBuilder.getCurrentPage(player);
        Set<String> pages = guiBuilder.getAvailablePages();
        List<String> pageList = new ArrayList<>(pages);
        Collections.sort(pageList);

        int currentIndex = pageList.indexOf(currentPage);
        if (currentIndex >= 0 && currentIndex < pageList.size() - 1) {
            String nextPage = pageList.get(currentIndex + 1);
            guiBuilder.setChangingPage(player, true);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                guiBuilder.openGui(player, nextPage);
            }, 1L);
        }
    }

    private void previousPage(Player player) {
        String currentPage = guiBuilder.getCurrentPage(player);
        Set<String> pages = guiBuilder.getAvailablePages();
        List<String> pageList = new ArrayList<>(pages);
        Collections.sort(pageList);

        int currentIndex = pageList.indexOf(currentPage);
        if (currentIndex > 0) {
            String prevPage = pageList.get(currentIndex - 1);
            guiBuilder.setChangingPage(player, true);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                guiBuilder.openGui(player, prevPage);
            }, 1L);
        }
    }

    private void leaveRoom(Player player) {
        Room currentRoom = roomService.getRoomByMember(player);
        if (currentRoom == null) {
            player.sendMessage(ComponentUtil.updateString(messages.getLeaveNotInRoom()));
            player.closeInventory();
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
                newOwner.sendMessage(ComponentUtil.updateString(messages.getOwnerTransfer(placeholders)));
            }
        }

        playerRoomManager.removePlayer(player);
        guiBuilder.refreshAllGuis();

        if (previousLocation != null && previousLocation.getWorld() != null) {
            player.teleport(previousLocation);
        } else {
            player.performCommand("spawn");
        }

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("room", currentRoom.getRoomName());
        player.sendMessage(ComponentUtil.updateString(messages.getLeaveSuccess(placeholders)));
        player.closeInventory();
    }

    private void handleRoomJoin(Player player, String roomName) {
        Room room = roomService.getRoom(roomName);
        if (room == null) {
            player.sendMessage(ComponentUtil.updateString(messages.getJoinRoomNotFound()));
            player.closeInventory();
            return;
        }

        Room currentRoom = roomService.getRoomByMember(player);
        if (currentRoom != null) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("room", currentRoom.getRoomName());
            player.sendMessage(ComponentUtil.updateString(messages.getJoinAlreadyInRoom(placeholders)));
            player.closeInventory();
            return;
        }

        if (!room.canJoin(player)) {
            if (room.isPrivate()) player.sendMessage(ComponentUtil.updateString(messages.getJoinPrivate()));
            else player.sendMessage(ComponentUtil.updateString(messages.getJoinNoPermission()));
            player.closeInventory();
            return;
        }

        if (room.isFull() && !player.hasPermission(Loader.getInstance().getConfigManager().getForceJoinPermission())) {
            player.sendMessage(ComponentUtil.updateString(messages.getJoinFull()));
            player.closeInventory();
            return;
        }

        if (roomService.joinRoom(player, roomName)) {
            playerRoomManager.setPreviousLocation(player, player.getLocation());
            playerRoomManager.setPlayerRoom(player, roomName);
            player.teleport(room.getSpawnPoint());
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("room", room.getRoomName());
            player.sendMessage(ComponentUtil.updateString(messages.getJoinSuccess(placeholders)));
            guiBuilder.refreshAllGuis();
            player.closeInventory();
        } else {
            player.sendMessage(ComponentUtil.updateString(messages.getJoinError()));
        }
    }
}