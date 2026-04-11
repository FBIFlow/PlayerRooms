package org.bestraxstudio.playerrooms.manager;

import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerRoomManager {

    private final Map<UUID, String> playerRoomMap;

    public PlayerRoomManager() { this.playerRoomMap = new HashMap<>(); }

    public void setPlayerRoom(Player player, String roomName) {
        if (roomName == null) playerRoomMap.remove(player.getUniqueId());
        else playerRoomMap.put(player.getUniqueId(), roomName);
    }

    public String getPlayerRoom(Player player) { return playerRoomMap.get(player.getUniqueId()); }
    public boolean isInRoom(Player player) { return playerRoomMap.containsKey(player.getUniqueId()); }
    public void removePlayer(Player player) { playerRoomMap.remove(player.getUniqueId()); }
    public void clearAll() { playerRoomMap.clear(); }
}