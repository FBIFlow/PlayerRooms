package org.bestraxstudio.playerrooms.model;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

public class Room {

    private String roomName;
    private Location minLocation, maxLocation;
    private Location spawnLocation;

    private Player owner;
    private List<Player> members;
    private int maxPlayers;
    private int currentPlayers;

    public Room(String roomName, Location minLocation, Location maxLocation, Location spawnLocation) {
        this.roomName = roomName;
        this.minLocation = minLocation;
        this.maxLocation = maxLocation;
        this.spawnLocation = spawnLocation;
    }

    public Player getOwner() {
        return owner;
    }

    public void setOwner(Player owner) {
        this.owner = owner;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public Location getMinLocation() {
        return minLocation;
    }

    public void setMinLocation(Location minLocation) {
        this.minLocation = minLocation;
    }

    public Location getMaxLocation() {
        return maxLocation;
    }

    public void setMaxLocation(Location maxLocation) {
        this.maxLocation = maxLocation;
    }

    public Location getSpawnLocation() {
        return spawnLocation;
    }

    public void setSpawnLocation(Location spawnLocation) {
        this.spawnLocation = spawnLocation;
    }

    public List<Player> getMembers() {
        return members;
    }

    public void setMembers(List<Player> members) {
        this.members = members;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public int getCurrentPlayers() {
        return currentPlayers;
    }

    public void setCurrentPlayers(int currentPlayers) {
        this.currentPlayers = currentPlayers;
    }
}