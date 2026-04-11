package org.bestraxstudio.playerrooms.model;

import org.bukkit.entity.Player;

import java.util.UUID;

public class Invitation {

    private final UUID inviter;
    private final String inviterName;
    private final String roomName;
    private final long expiryTime;

    public Invitation(Player inviter, String roomName) {
        this.inviter = inviter.getUniqueId();
        this.inviterName = inviter.getName();
        this.roomName = roomName;
        this.expiryTime = System.currentTimeMillis() + 30000;
    }

    public UUID getInviter() { return inviter; }
    public String getInviterName() { return inviterName; }
    public String getRoomName() { return roomName; }
    public boolean isExpired() { return System.currentTimeMillis() > expiryTime; }
    public long getExpiryTime() { return expiryTime; }
}