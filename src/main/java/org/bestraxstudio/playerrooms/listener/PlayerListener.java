package org.bestraxstudio.playerrooms.listener;

import org.bestraxstudio.playerrooms.Loader;
import org.bestraxstudio.playerrooms.gui.GuiBuilder;
import org.bestraxstudio.playerrooms.manager.InvitationManager;
import org.bestraxstudio.playerrooms.manager.PlayerRoomManager;
import org.bestraxstudio.playerrooms.model.Room;
import org.bestraxstudio.playerrooms.service.RoomService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private final Loader plugin;
    private final RoomService roomService;
    private final PlayerRoomManager playerRoomManager;
    private final InvitationManager invitationManager;
    private final GuiBuilder guiBuilder;

    public PlayerListener(Loader plugin) {
        this.plugin = plugin;
        this.roomService = plugin.getRoomService();
        this.playerRoomManager = plugin.getPlayerRoomManager();
        this.invitationManager = plugin.getInvitationManager();
        this.guiBuilder = plugin.getGuiBuilder();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Room currentRoom = roomService.getRoomByMember(player);
        if (currentRoom != null) {
            roomService.leaveCurrentRoom(player);
            playerRoomManager.removePlayer(player);
            guiBuilder.refreshAllGuis();
        }
        guiBuilder.removePlayer(player);
        invitationManager.removeAllInvitations(player);
    }
}