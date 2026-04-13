package org.bestraxstudio.playerrooms.manager;

import org.bukkit.entity.Player;
import java.util.*;

public class IgnoreListManager {

    private final Map<UUID, Set<UUID>> ignoreList;

    public IgnoreListManager() {
        this.ignoreList = new HashMap<>();
    }

    public void addIgnore(Player player, Player target) {
        ignoreList.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>()).add(target.getUniqueId());
    }

    public void removeIgnore(Player player, Player target) {
        Set<UUID> ignores = ignoreList.get(player.getUniqueId());
        if (ignores != null) {
            ignores.remove(target.getUniqueId());
            if (ignores.isEmpty()) {
                ignoreList.remove(player.getUniqueId());
            }
        }
    }

    public boolean isIgnored(Player player, Player target) {
        Set<UUID> ignores = ignoreList.get(player.getUniqueId());
        return ignores != null && ignores.contains(target.getUniqueId());
    }

    public List<String> getIgnoreList(Player player) {
        Set<UUID> ignores = ignoreList.get(player.getUniqueId());
        if (ignores == null || ignores.isEmpty()) return new ArrayList<>();
        List<String> names = new ArrayList<>();
        for (UUID uuid : ignores) {
            Player target = org.bukkit.Bukkit.getPlayer(uuid);
            if (target != null) {
                names.add(target.getName());
            }
        }
        return names;
    }

    public void removeAll(Player player) {
        ignoreList.remove(player.getUniqueId());
    }

    public void clearAll() {
        ignoreList.clear();
    }
}