package org.bestraxstudio.playerrooms.service;

import org.bestraxstudio.playerrooms.Loader;
import org.bestraxstudio.playerrooms.config.ConfigManager;
import org.bestraxstudio.playerrooms.model.Room;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RoomService {

    private final Loader plugin;
    private final Map<String, Room> rooms;

    public RoomService(Loader plugin) {
        this.plugin = plugin;
        this.rooms = new HashMap<>();
    }

    public void loadRoomsFromConfig() {
        rooms.clear();
        ConfigManager configManager = plugin.getConfigManager();
        for (String roomName : configManager.getRoomNames()) {
            try {
                Room room = loadRoom(configManager, roomName);
                if (room != null) rooms.put(roomName.toLowerCase(), room);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load room '" + roomName + "': " + e.getMessage());
            }
        }
    }

    private Room loadRoom(ConfigManager config, String roomName) {
        Location spawnPoint = config.getRoomSpawnPoint(roomName);
        if (spawnPoint == null) throw new IllegalArgumentException("Invalid spawn point");
        int maxPlayersDefault = config.getRoomMaxPlayersDefault(roomName);
        String joinPermission = config.getRoomJoinPermission(roomName);
        String makePrivatePermission = config.getRoomMakePrivatePermission(roomName);
        Location minBounds = config.hasRoomBounds(roomName) ? config.getRoomMin(roomName) : null;
        Location maxBounds = config.hasRoomBounds(roomName) ? config.getRoomMax(roomName) : null;
        return new Room(roomName, spawnPoint, minBounds, maxBounds, maxPlayersDefault, joinPermission, makePrivatePermission);
    }

    public Room getRoom(String roomName) { return rooms.get(roomName.toLowerCase()); }
    public Room getRoomByMember(Player player) {
        for (Room room : rooms.values()) if (room.isMember(player)) return room;
        return null;
    }
    public Map<String, Room> getAllRooms() { return new HashMap<>(rooms); }
    public boolean joinRoom(Player player, String roomName) {
        Room room = getRoom(roomName);
        return room != null && room.canJoin(player) && room.addMember(player);
    }
    public boolean leaveRoom(Player player, String roomName) {
        Room room = getRoom(roomName);
        return room != null && room.removeMember(player);
    }
    public boolean leaveCurrentRoom(Player player) {
        Room currentRoom = getRoomByMember(player);
        return currentRoom != null && currentRoom.removeMember(player);
    }
    public boolean setRoomPrivate(Player player, String roomName, boolean isPrivate) {
        Room room = getRoom(roomName);
        if (room == null) return false;
        String requiredPerm = room.getMakePrivatePermission();
        if (!requiredPerm.equalsIgnoreCase("all") && !player.hasPermission(requiredPerm)) return false;
        if (!room.isOwner(player)) return false;
        room.setPrivate(isPrivate);
        return true;
    }
    public boolean setRoomOwner(Player player, String roomName) {
        Room room = getRoom(roomName);
        return room != null && room.getOwner() == null && room.addMember(player);
    }
    public boolean setRoomMaxPlayers(Player player, String roomName, int maxPlayers) {
        Room room = getRoom(roomName);
        if (room == null || !room.isOwner(player)) return false;
        if (maxPlayers < 1 || maxPlayers > 100) return false;
        room.setMaxPlayers(maxPlayers);
        return true;
    }
    public boolean kickFromRoom(Player owner, Player target, String roomName) {
        Room room = getRoom(roomName);
        if (room == null || !room.isOwner(owner) || room.isOwner(target)) return false;
        return room.removeMember(target);
    }
    public boolean forceJoin(Player target, String roomName) {
        Room room = getRoom(roomName);
        return room != null && room.addMember(target, true);
    }
}