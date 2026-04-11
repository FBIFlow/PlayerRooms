package org.bestraxstudio.playerrooms.manager;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerRoomManager {

    private final Map<UUID, String> playerRoomMap;
    private final Map<UUID, Location> previousLocationMap;

    public PlayerRoomManager() {
        this.playerRoomMap = new HashMap<>();
        this.previousLocationMap = new HashMap<>();
    }

    public void setPlayerRoom(Player player, String roomName) {
        if (roomName == null) {
            playerRoomMap.remove(player.getUniqueId());
        } else {
            playerRoomMap.put(player.getUniqueId(), roomName);
        }
    }

    public void setPreviousLocation(Player player, Location location) {
        if (location != null) {
            previousLocationMap.put(player.getUniqueId(), location.clone());
        }
    }

    public Location getPreviousLocation(Player player) {
        return previousLocationMap.get(player.getUniqueId());
    }

    public void removePreviousLocation(Player player) {
        previousLocationMap.remove(player.getUniqueId());
    }

    public String getPlayerRoom(Player player) {
        return playerRoomMap.get(player.getUniqueId());
    }

    public boolean isInRoom(Player player) {
        return playerRoomMap.containsKey(player.getUniqueId());
    }

    public void removePlayer(Player player) {
        playerRoomMap.remove(player.getUniqueId());
        previousLocationMap.remove(player.getUniqueId());
    }

    public void clearAll() {
        playerRoomMap.clear();
        previousLocationMap.clear();
    }
}