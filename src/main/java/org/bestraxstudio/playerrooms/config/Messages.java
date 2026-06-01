package org.bestraxstudio.playerrooms.config;

import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.ChatColor;
import org.bestraxstudio.playerrooms.util.ComponentUtil;
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

    private Component getMessage(String key) {
        if (messageCache.containsKey(key)) return ComponentUtil.updateString(messageCache.get(key));
        String message = messagesConfig.getString(key, "&cMessage not found: " + key);
        String colored = ChatColor.translateAlternateColorCodes('&', message);
        messageCache.put(key, colored);
        return ComponentUtil.updateString(colored);
    }
    
    private String getMessageString(String key) {
        if (messageCache.containsKey(key)) return messageCache.get(key);
        String message = messagesConfig.getString(key, "&cMessage not found: " + key);
        String colored = ChatColor.translateAlternateColorCodes('&', message);
        messageCache.put(key, colored);
        return colored;
    }

    private Component getMessage(String key, Map<String, Component> placeholders) {
        String message = getMessageString(key);
        for (Map.Entry<String, Component> entry : placeholders.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue().toString());
        }
        return ComponentUtil.updateString(message);
    }

    public void reload() { loadMessages(); }

    public Component getGuiNoPages() { return getMessage("gui.no-pages"); }

    public Component getJoinSuccess(Map<String, Component> placeholders) { return getMessage("join.success", placeholders); }
    public Component getJoinFull() { return getMessage("join.full"); }
    public Component getJoinNoPermission() { return getMessage("join.no-permission"); }
    public Component getJoinPrivate() { return getMessage("join.private"); }
    public Component getJoinAlreadyInRoom(Map<String, Component> placeholders) { return getMessage("join.already-in-room", placeholders); }
    public Component getJoinRoomNotFound() { return getMessage("join.room-not-found"); }
    public Component getJoinError() { return getMessage("join.error"); }

    public Component getLeaveSuccess(Map<String, Component> placeholders) { return getMessage("leave.success", placeholders); }
    public Component getLeaveNotInRoom() { return getMessage("leave.not-in-room"); }
    public Component getLeaveBoundsExit(Map<String, Component> placeholders) { return getMessage("leave.bounds-exit", placeholders); }

    public Component getInfoNotInRoom() { return getMessage("info.not-in-room"); }
    public Component getInfoCurrent(Map<String, Component> placeholders) { return getMessage("info.current", placeholders); }

    public Component getCommandUnknown() { return getMessage("command.unknown"); }
    public Component getCommandOnlyPlayers() { return getMessage("command.only-players"); }
    public Component getCommandNoPermission() { return getMessage("command.no-permission"); }
    public Component getCommandReloadSuccess() { return getMessage("command.reload-success"); }
    public Component getCommandBlocked() { return getMessage("command.blocked"); }

    public Component getProtectionCannotBreak() { return getMessage("protection.cannot-break"); }
    public Component getProtectionCannotPlace() { return getMessage("protection.cannot-place"); }
    public Component getProtectionCannotInteract() { return getMessage("protection.cannot-interact"); }
    public Component getProtectionCannotUse() { return getMessage("protection.cannot-use"); }
    public Component getProtectionPvpDisabled() { return getMessage("protection.pvp-disabled"); }

    public Component getSetprivateInvalidArgument() { return getMessage("setprivate.invalid-argument"); }
    public Component getSetprivateOpenedState() { return getMessage("setprivate.opened-state"); }
    public Component getSetprivateClosedState() { return getMessage("setprivate.closed-state"); }
    public Component getSetprivateNotInRoom() { return getMessage("setprivate.not-in-room"); }
    public Component getSetprivateNotOwner() { return getMessage("setprivate.not-owner"); }
    public Component getSetprivateNoPermission() { return getMessage("setprivate.no-permission"); }
    public Component getSetprivateSuccess(Map<String, Component> placeholders) { 
        return getMessage("setprivate.success", placeholders); 
    }

    public Component getInviteUsage() { return getMessage("invite.usage"); }
    public Component getInviteNotInRoom() { return getMessage("invite.not-in-room"); }
    public Component getInviteNotOwner() { return getMessage("invite.not-owner"); }
    public Component getInvitePlayerNotFound() { return getMessage("invite.player-not-found"); }
    public Component getInviteCannotInviteSelf() { return getMessage("invite.cannot-invite-self"); }
    public Component getInviteAlreadyInRoom(Map<String, Component> placeholders) { return getMessage("invite.already-in-room", placeholders); }
    public Component getInvitePlayerIgnored() { return getMessage("invite.player-ignored"); }
    public Component getInviteSent(Map<String, Component> placeholders) { return getMessage("invite.sent", placeholders); }
    public Component getInviteReceived(Map<String, Component> placeholders) { return getMessage("invite.received", placeholders); }
    public Component getInviteClickToAccept(Map<String, Component> placeholders) { return getMessage("invite.click-to-accept", placeholders); }
    public Component getInvitePlayerJoined(Map<String, Component> placeholders) { return getMessage("invite.player-joined", placeholders); }

    public Component getAcceptAlreadyInRoom() { return getMessage("accept.already-in-room"); }
    public Component getAcceptNoInvitations() { return getMessage("accept.no-invitations"); }
    public Component getAcceptInvitationNotFound(Map<String, Component> placeholders) { return getMessage("accept.invitation-not-found", placeholders); }
    public Component getAcceptInvitationExpired() { return getMessage("accept.invitation-expired"); }
    public Component getAcceptInviterOffline() { return getMessage("accept.inviter-offline"); }
    public Component getAcceptRoomNotFound() { return getMessage("accept.room-not-found"); }

    public Component getIgnoreUsage() { return getMessage("ignore.usage"); }
    public Component getIgnorePlayerNotFound() { return getMessage("ignore.player-not-found"); }
    public Component getIgnoreCannotIgnoreSelf() { return getMessage("ignore.cannot-ignore-self"); }
    public Component getIgnoreAlreadyIgnored(Map<String, Component> placeholders) { return getMessage("ignore.already-ignored", placeholders); }
    public Component getIgnoreSuccess(Map<String, Component> placeholders) { return getMessage("ignore.success", placeholders); }
    public Component getIgnoreListEmpty() { return getMessage("ignore.list-empty"); }
    public Component getIgnoreListHeader() { return getMessage("ignore.list-header"); }
    public Component getIgnoreListEntry(Map<String, Component> placeholders) { return getMessage("ignore.list-entry", placeholders); }

    public Component getUnignoreUsage() { return getMessage("unignore.usage"); }
    public Component getUnignorePlayerNotFound() { return getMessage("unignore.player-not-found"); }
    public Component getUnignoreNotIgnored(Map<String, Component> placeholders) { return getMessage("unignore.not-ignored", placeholders); }
    public Component getUnignoreSuccess(Map<String, Component> placeholders) { return getMessage("unignore.success", placeholders); }

    public Component getPluginEnabled(Map<String, Component> placeholders) { return getMessage("plugin.enabled", placeholders); }
    public Component getPluginDisabled() { return getMessage("plugin.disabled"); }

    public Component getGeneratorConfigNotFound() { return getMessage("generator.config-not-found"); }
    public Component getGeneratorWorldNotFound() { return getMessage("generator.world-not-found"); }
    public Component getGeneratorMissingBounds(Map<String, Component> placeholders) { return getMessage("generator.missing-bounds", placeholders); }
    public Component getGeneratorRoomGenerated(Map<String, Component> placeholders) { return getMessage("generator.room-generated", placeholders); }
    public Component getGeneratorAllGenerated() { return getMessage("generator.all-generated"); }

    public Component getOwnerTransfer(Map<String, Component> placeholders) { return getMessage("owner.transfer", placeholders); }
}