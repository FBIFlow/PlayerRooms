package org.bestraxstudio.playerrooms.model;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Room {

    private final String roomName;
    private final Location spawnPoint;
    private final Location minBounds;
    private final Location maxBounds;
    private final int maxPlayersDefault;
    private final String joinPermission;
    private final String makePrivatePermission;
    private final Set<UUID> members;
    private UUID owner;
    private boolean isPrivate;
    private int customMaxPlayers;

    public Room(String roomName, Location spawnPoint, Location minBounds, Location maxBounds,
                int maxPlayersDefault, String joinPermission, String makePrivatePermission) {
        this.roomName = roomName;
        this.spawnPoint = spawnPoint;
        this.minBounds = minBounds;
        this.maxBounds = maxBounds;
        this.maxPlayersDefault = maxPlayersDefault;
        this.joinPermission = joinPermission;
        this.makePrivatePermission = makePrivatePermission;
        this.members = new HashSet<>();
        this.isPrivate = false;
        this.customMaxPlayers = maxPlayersDefault;
    }

    public String getRoomName() { return roomName; }
    public Location getSpawnPoint() { return spawnPoint.clone(); }
    public boolean hasBounds() { return minBounds != null && maxBounds != null; }

    public boolean isInsideBounds(Location location) {
        if (!hasBounds()) return true;
        if (!location.getWorld().equals(minBounds.getWorld())) return false;
        double x = location.getX(), y = location.getY(), z = location.getZ();
        return x >= minBounds.getX() && x <= maxBounds.getX() &&
                y >= minBounds.getY() && y <= maxBounds.getY() &&
                z >= minBounds.getZ() && z <= maxBounds.getZ();
    }

    public String getJoinPermission() { return joinPermission; }
    public String getMakePrivatePermission() { return makePrivatePermission; }
    public int getMaxPlayers() { return customMaxPlayers; }
    public void setMaxPlayers(int maxPlayers) { this.customMaxPlayers = maxPlayers; }
    public int getMaxPlayersDefault() { return maxPlayersDefault; }
    public int getCurrentPlayers() { return members.size(); }
    public boolean isFull() { return members.size() >= customMaxPlayers; }
    public boolean isPrivate() { return isPrivate; }
    public void setPrivate(boolean isPrivate) { this.isPrivate = isPrivate; }
    public UUID getOwner() { return owner; }
    public void setOwner(Player owner) { this.owner = owner != null ? owner.getUniqueId() : null; }
    public boolean isOwner(Player player) { return owner != null && owner.equals(player.getUniqueId()); }

    public boolean canJoin(Player player) {
        if (isPrivate && !isOwner(player)) return false;
        return joinPermission.equalsIgnoreCase("all") || player.hasPermission(joinPermission);
    }

    public boolean addMember(Player player) { return addMember(player, false); }

    public boolean addMember(Player player, boolean force) {
        if (!force) {
            if (isPrivate && !isOwner(player)) return false;
            if (!joinPermission.equalsIgnoreCase("all") && !player.hasPermission(joinPermission)) return false;
            if (isFull()) return false;
        }
        boolean added = members.add(player.getUniqueId());
        if (added && members.size() == 1 && owner == null) this.owner = player.getUniqueId();
        return added;
    }

    public boolean removeMember(Player player) {
        boolean removed = members.remove(player.getUniqueId());
        if (removed && isOwner(player) && !members.isEmpty()) this.owner = members.iterator().next();
        if (members.isEmpty()) {
            this.isPrivate = false;
            this.owner = null;
            this.customMaxPlayers = this.maxPlayersDefault;
        }
        return removed;
    }

    public boolean isMember(Player player) { return members.contains(player.getUniqueId()); }
    public Set<UUID> getMembers() { return new HashSet<>(members); }
    public Location getMinBounds() { return minBounds != null ? minBounds.clone() : null; }
    public Location getMaxBounds() { return maxBounds != null ? maxBounds.clone() : null; }
}