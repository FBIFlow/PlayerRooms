package org.bestraxstudio.playerrooms.manager;

import org.bestraxstudio.playerrooms.model.Invitation;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class InvitationManager {

    private final Map<UUID, List<Invitation>> playerInvitations;

    public InvitationManager() {
        this.playerInvitations = new HashMap<>();
    }

    public void createInvitation(Player inviter, Player invited, String roomName) {
        Invitation invitation = new Invitation(inviter, roomName);
        playerInvitations.computeIfAbsent(invited.getUniqueId(), k -> new ArrayList<>()).add(invitation);
        Bukkit.getScheduler().runTaskLaterAsynchronously(Bukkit.getPluginManager().getPlugin("PlayerRooms"), () -> {
            removeExpiredInvitations(invited.getUniqueId());
        }, 600L);
    }

    public Invitation getLatestInvitation(Player player) {
        List<Invitation> invitations = playerInvitations.get(player.getUniqueId());
        if (invitations == null || invitations.isEmpty()) return null;
        removeExpiredInvitations(player.getUniqueId());
        invitations = playerInvitations.get(player.getUniqueId());
        if (invitations == null || invitations.isEmpty()) return null;
        return invitations.get(invitations.size() - 1);
    }

    public Invitation getInvitationFrom(Player player, String inviterName) {
        List<Invitation> invitations = playerInvitations.get(player.getUniqueId());
        if (invitations == null || invitations.isEmpty()) return null;
        removeExpiredInvitations(player.getUniqueId());
        invitations = playerInvitations.get(player.getUniqueId());
        if (invitations == null || invitations.isEmpty()) return null;
        for (Invitation inv : invitations) {
            if (inv.getInviterName().equalsIgnoreCase(inviterName)) {
                return inv;
            }
        }
        return null;
    }

    public void removeInvitation(Player player, Invitation invitation) {
        List<Invitation> invitations = playerInvitations.get(player.getUniqueId());
        if (invitations != null) {
            invitations.remove(invitation);
            if (invitations.isEmpty()) {
                playerInvitations.remove(player.getUniqueId());
            }
        }
    }

    public void removeInvitationsFrom(Player inviter, Player invited) {
        List<Invitation> invitations = playerInvitations.get(invited.getUniqueId());
        if (invitations != null) {
            invitations.removeIf(inv -> inv.getInviter().equals(inviter.getUniqueId()));
            if (invitations.isEmpty()) {
                playerInvitations.remove(invited.getUniqueId());
            }
        }
    }

    public void removeAllInvitations(Player player) {
        playerInvitations.remove(player.getUniqueId());
    }

    private void removeExpiredInvitations(UUID playerId) {
        List<Invitation> invitations = playerInvitations.get(playerId);
        if (invitations == null) return;
        invitations.removeIf(Invitation::isExpired);
        if (invitations.isEmpty()) {
            playerInvitations.remove(playerId);
        }
    }

    public void cleanup() {
        for (UUID playerId : new HashSet<>(playerInvitations.keySet())) {
            removeExpiredInvitations(playerId);
        }
    }
}