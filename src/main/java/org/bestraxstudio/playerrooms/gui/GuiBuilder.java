package org.bestraxstudio.playerrooms.gui;

import net.md_5.bungee.api.ChatColor;
import org.bestraxstudio.playerrooms.Loader;
import org.bestraxstudio.playerrooms.config.ConfigManager;
import org.bestraxstudio.playerrooms.model.Room;
import org.bestraxstudio.playerrooms.service.RoomService;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GuiBuilder implements Listener {

    private final Loader plugin;
    private final ConfigManager configManager;
    private final RoomService roomService;
    private final Map<UUID, String> playerPageMap;
    private final Set<UUID> playersChangingPage;
    private static final Pattern HEX_PATTERN = Pattern.compile("#[a-fA-F0-9]{6}");

    public GuiBuilder(Loader plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.roomService = plugin.getRoomService();
        this.playerPageMap = new HashMap<>();
        this.playersChangingPage = new HashSet<>();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void openGui(Player player, String pageId) {
        playerPageMap.put(player.getUniqueId(), pageId);
        Inventory inventory = buildPage(player, pageId);
        player.openInventory(inventory);
    }

    public void refreshGui(Player player) {
        String currentPage = playerPageMap.get(player.getUniqueId());
        if (currentPage != null && player.getOpenInventory() != null) {
            Inventory newInventory = buildPage(player, currentPage);
            player.openInventory(newInventory);
        }
    }

    public void refreshAllGuis() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (playerPageMap.containsKey(player.getUniqueId()) && player.getOpenInventory() != null) {
                refreshGui(player);
            }
        }
    }

    public void setChangingPage(Player player, boolean changing) {
        if (changing) {
            playersChangingPage.add(player.getUniqueId());
        } else {
            playersChangingPage.remove(player.getUniqueId());
        }
    }

    private Inventory buildPage(Player player, String pageId) {
        String title = configManager.getPageName(pageId);
        int size = configManager.getPageSize(pageId);
        Inventory inventory = Bukkit.createInventory(null, size, colorize(title));
        ConfigurationSection slots = configManager.getPageSlots(pageId);
        if (slots == null) return inventory;

        for (String slotKey : slots.getKeys(false)) {
            try {
                int slot = Integer.parseInt(slotKey);
                ItemStack item = buildSlotItem(player, pageId, slot);
                if (item != null) inventory.setItem(slot, item);
            } catch (NumberFormatException ignored) {}
        }
        return inventory;
    }

    private ItemStack buildSlotItem(Player player, String pageId, int slot) {
        String materialName = configManager.getSlotMaterial(pageId, slot);
        Material material = Material.getMaterial(materialName);
        if (material == null) material = Material.BARRIER;

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        String rawName = configManager.getSlotName(pageId, slot);
        String processedName = processPlaceholders(player, pageId, slot, rawName);
        meta.setDisplayName(colorize(processedName));

        List<String> rawLore = configManager.getSlotLore(pageId, slot);
        List<String> processedLore = new ArrayList<>();
        for (String line : rawLore) {
            processedLore.add(colorize(processPlaceholders(player, pageId, slot, line)));
        }
        if (!processedLore.isEmpty()) meta.setLore(processedLore);

        item.setItemMeta(meta);
        return item;
    }

    private String processPlaceholders(Player player, String pageId, int slot, String text) {
        String linkedRoom = configManager.getSlotLinkedRoom(pageId, slot);
        Room room = linkedRoom != null ? roomService.getRoom(linkedRoom) : null;

        boolean isBusy = room != null && room.isFull();
        String busyColor = configManager.getBusyStateColor(isBusy);
        String busyText = configManager.getBusyStateText(isBusy);

        boolean isPrivate = room != null && room.isPrivate();
        String privateColor = configManager.getPrivateStateColor(isPrivate);
        String privateText = configManager.getPrivateStateText(isPrivate);

        text = text.replace("%busy_color%", busyColor)
                .replace("%busy_text%", busyText)
                .replace("%private_color%", privateColor)
                .replace("%private_text%", privateText);

        if (room != null) {
            text = text.replace("%current_player%", String.valueOf(room.getCurrentPlayers()))
                    .replace("%max_players%", String.valueOf(room.getMaxPlayers()));
        }
        return text;
    }

    private String colorize(String text) {
        if (text == null) return "";
        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String hex = matcher.group();
            matcher.appendReplacement(buffer, ChatColor.of(hex).toString());
        }
        matcher.appendTail(buffer);
        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();

        if (playersChangingPage.contains(player.getUniqueId())) {
            playersChangingPage.remove(player.getUniqueId());
            return;
        }

        playerPageMap.remove(player.getUniqueId());
    }

    public String getCurrentPage(Player player) {
        return playerPageMap.get(player.getUniqueId());
    }

    public Set<String> getAvailablePages() {
        return configManager.getPageNames();
    }

    public void removePlayer(Player player) {
        playerPageMap.remove(player.getUniqueId());
        playersChangingPage.remove(player.getUniqueId());
    }
}