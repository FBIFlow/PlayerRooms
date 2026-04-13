package org.bestraxstudio.playerrooms.config;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Messages {

    private final JavaPlugin plugin;
    private YamlConfiguration messagesConfig;
    private final Map<String, String> messageCache;

    public Messages(JavaPlugin plugin) {
        this.plugin = plugin;
        this.messageCache = new HashMap<>();
        loadMessages();
    }

    public void loadMessages() {
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) plugin.saveResource("messages.yml", false);
        this.messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        messageCache.clear();
    }

    private String getMessage(String key) {
        if (messageCache.containsKey(key)) return messageCache.get(key);
        String message = messagesConfig.getString(key, "&cMessage not found: " + key);
        String colored = ChatColor.translateAlternateColorCodes('&', message);
        messageCache.put(key, colored);
        return colored;
    }

    private String getMessage(String key, Map<String, String> placeholders) {
        String message = getMessage(key);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return message;
    }

    public void reload() { loadMessages(); }

    public String getGuiNoPages() { return getMessage("gui.no-pages"); }

    public String getJoinSuccess(Map<String, String> placeholders) { return getMessage("join.success", placeholders); }
    public String getJoinFull() { return getMessage("join.full"); }
    public String getJoinNoPermission() { return getMessage("join.no-permission"); }
    public String getJoinPrivate() { return getMessage("join.private"); }
    public String getJoinAlreadyInRoom(Map<String, String> placeholders) { return getMessage("join.already-in-room", placeholders); }
    public String getJoinRoomNotFound() { return getMessage("join.room-not-found"); }
    public String getJoinError() { return getMessage("join.error"); }

    public String getLeaveSuccess(Map<String, String> placeholders) { return getMessage("leave.success", placeholders); }
    public String getLeaveNotInRoom() { return getMessage("leave.not-in-room"); }
    public String getLeaveBoundsExit(Map<String, String> placeholders) { return getMessage("leave.bounds-exit", placeholders); }

    public String getInfoNotInRoom() { return getMessage("info.not-in-room"); }
    public String getInfoCurrent(Map<String, String> placeholders) { return getMessage("info.current", placeholders); }

    public String getCommandUnknown() { return getMessage("command.unknown"); }
    public String getCommandOnlyPlayers() { return getMessage("command.only-players"); }
    public String getCommandNoPermission() { return getMessage("command.no-permission"); }
    public String getCommandReloadSuccess() { return getMessage("command.reload-success"); }
    public String getCommandBlocked() { return getMessage("command.blocked"); }

    public String getProtectionCannotBreak() { return getMessage("protection.cannot-break"); }
    public String getProtectionCannotPlace() { return getMessage("protection.cannot-place"); }
    public String getProtectionCannotInteract() { return getMessage("protection.cannot-interact"); }
    public String getProtectionCannotUse() { return getMessage("protection.cannot-use"); }
    public String getProtectionPvpDisabled() { return getMessage("protection.pvp-disabled"); }

    public String getSetprivateInvalidArgument() { return getMessage("setprivate.invalid-argument"); }
    public String getSetprivateOpenedState() { return getMessage("setprivate.opened-state"); }
    public String getSetprivateClosedState() { return getMessage("setprivate.closed-state"); }
    public String getSetprivateNotInRoom() { return getMessage("setprivate.not-in-room"); }
    public String getSetprivateNotOwner() { return getMessage("setprivate.not-owner"); }
    public String getSetprivateNoPermission() { return getMessage("setprivate.no-permission"); }
    public String getSetprivateSuccess(Map<String, String> placeholders) { return getMessage("setprivate.success", placeholders); }

    public String getInviteUsage() { return getMessage("invite.usage"); }
    public String getInviteNotInRoom() { return getMessage("invite.not-in-room"); }
    public String getInviteNotOwner() { return getMessage("invite.not-owner"); }
    public String getInvitePlayerNotFound() { return getMessage("invite.player-not-found"); }
    public String getInviteCannotInviteSelf() { return getMessage("invite.cannot-invite-self"); }
    public String getInviteAlreadyInRoom(Map<String, String> placeholders) { return getMessage("invite.already-in-room", placeholders); }
    public String getInvitePlayerIgnored() { return getMessage("invite.player-ignored"); }
    public String getInviteSent(Map<String, String> placeholders) { return getMessage("invite.sent", placeholders); }
    public String getInviteReceived(Map<String, String> placeholders) { return getMessage("invite.received", placeholders); }
    public String getInviteClickToAccept(Map<String, String> placeholders) { return getMessage("invite.click-to-accept", placeholders); }
    public String getInvitePlayerJoined(Map<String, String> placeholders) { return getMessage("invite.player-joined", placeholders); }

    public String getAcceptAlreadyInRoom() { return getMessage("accept.already-in-room"); }
    public String getAcceptNoInvitations() { return getMessage("accept.no-invitations"); }
    public String getAcceptInvitationNotFound(Map<String, String> placeholders) { return getMessage("accept.invitation-not-found", placeholders); }
    public String getAcceptInvitationExpired() { return getMessage("accept.invitation-expired"); }
    public String getAcceptInviterOffline() { return getMessage("accept.inviter-offline"); }
    public String getAcceptRoomNotFound() { return getMessage("accept.room-not-found"); }

    public String getIgnoreUsage() { return getMessage("ignore.usage"); }
    public String getIgnorePlayerNotFound() { return getMessage("ignore.player-not-found"); }
    public String getIgnoreCannotIgnoreSelf() { return getMessage("ignore.cannot-ignore-self"); }
    public String getIgnoreAlreadyIgnored(Map<String, String> placeholders) { return getMessage("ignore.already-ignored", placeholders); }
    public String getIgnoreSuccess(Map<String, String> placeholders) { return getMessage("ignore.success", placeholders); }
    public String getIgnoreListEmpty() { return getMessage("ignore.list-empty"); }
    public String getIgnoreListHeader() { return getMessage("ignore.list-header"); }
    public String getIgnoreListEntry(Map<String, String> placeholders) { return getMessage("ignore.list-entry", placeholders); }

    public String getUnignoreUsage() { return getMessage("unignore.usage"); }
    public String getUnignorePlayerNotFound() { return getMessage("unignore.player-not-found"); }
    public String getUnignoreNotIgnored(Map<String, String> placeholders) { return getMessage("unignore.not-ignored", placeholders); }
    public String getUnignoreSuccess(Map<String, String> placeholders) { return getMessage("unignore.success", placeholders); }

    public String getPluginEnabled(Map<String, String> placeholders) { return getMessage("plugin.enabled", placeholders); }
    public String getPluginDisabled() { return getMessage("plugin.disabled"); }

    public String getGeneratorConfigNotFound() { return getMessage("generator.config-not-found"); }
    public String getGeneratorWorldNotFound() { return getMessage("generator.world-not-found"); }
    public String getGeneratorMissingBounds(Map<String, String> placeholders) { return getMessage("generator.missing-bounds", placeholders); }
    public String getGeneratorRoomGenerated(Map<String, String> placeholders) { return getMessage("generator.room-generated", placeholders); }
    public String getGeneratorAllGenerated() { return getMessage("generator.all-generated"); }

    public String getOwnerTransfer(Map<String, String> placeholders) { return getMessage("owner.transfer", placeholders); }
}