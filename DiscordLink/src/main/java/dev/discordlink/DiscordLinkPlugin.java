package dev.discordlink;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;

public final class DiscordLinkPlugin extends JavaPlugin implements TabExecutor {

    private static final String PLACEHOLDER = "discord.gg/yourinvite";
    private static final MiniMessage MM = MiniMessage.miniMessage();

    @Override
    public void onEnable() {
        saveDefaultConfig();

        var command = getCommand("discord");
        if (command != null) {
            command.setExecutor(this);
            command.setTabCompleter(this);
        }

        String url = getLinkUrl();
        if (url == null) {
            getLogger().warning("No Discord link set! Edit plugins/DiscordLink/config.yml and run /discord reload.");
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        // /discord reload
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("discordlink.reload")) {
                sender.sendMessage(msg("messages.no-permission", "<red>You don't have permission to do that."));
                return true;
            }
            reloadConfig();
            sender.sendMessage(msg("messages.reloaded", "<green>DiscordLink config reloaded."));
            return true;
        }

        // /discord
        String url = getLinkUrl();
        if (url == null) {
            sender.sendMessage(msg("messages.not-set", "<red>The Discord link has not been set yet."));
            return true;
        }

        Component hover = msg("messages.hover", "<yellow>Click to open our Discord!");
        String linkFormat = getConfig().getString("messages.link-format", "<aqua><underlined>");

        Component link = MM.deserialize(linkFormat + "<text>", Placeholder.unparsed("text", url))
                .clickEvent(ClickEvent.openUrl(url))
                .hoverEvent(HoverEvent.showText(hover));

        String template = getConfig().getString("messages.discord", "<gray>Join our Discord: <link>");
        sender.sendMessage(MM.deserialize(template, Placeholder.component("link", link)));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1 && sender.hasPermission("discordlink.reload")
                && "reload".startsWith(args[0].toLowerCase(Locale.ROOT))) {
            return List.of("reload");
        }
        return List.of();
    }

    private Component msg(String path, String fallback) {
        return MM.deserialize(getConfig().getString(path, fallback));
    }

    /** Reads the link from config.yml and makes sure it starts with https://. Returns null if not set. */
    private String getLinkUrl() {
        String raw = getConfig().getString("discord-link", "").trim();
        if (raw.isEmpty() || raw.equalsIgnoreCase(PLACEHOLDER)) {
            return null;
        }
        if (!raw.regionMatches(true, 0, "http://", 0, 7) && !raw.regionMatches(true, 0, "https://", 0, 8)) {
            raw = "https://" + raw;
        }
        return raw;
    }
}
