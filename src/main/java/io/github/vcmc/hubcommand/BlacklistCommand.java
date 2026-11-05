package io.github.vcmc.hubcommand;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BlacklistCommand implements SimpleCommand {

    private static final List<String> SUBCOMMANDS = List.of("add", "remove", "list");

    private final ProxyServer server;
    private final PluginConfig config;

    public BlacklistCommand(ProxyServer server, PluginConfig config) {
        this.server = server;
        this.config = config;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (args.length < 1) {
            source.sendMessage(Component.text("Usage: /hubblock <add|remove|list> [server]", NamedTextColor.YELLOW));
            return;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "add" -> {
                if (args.length < 2) {
                    source.sendMessage(Component.text("Usage: /hubblock add <server>", NamedTextColor.YELLOW));
                    return;
                }
                String target = args[1];
                if (config.addBlockedServer(target)) {
                    source.sendMessage(Component.text("Added '" + target + "' to the blacklist.", NamedTextColor.GREEN));
                    warnIfNotSaved(source);
                } else {
                    source.sendMessage(Component.text("'" + target + "' is already in the blacklist.", NamedTextColor.RED));
                }
            }
            case "remove" -> {
                if (args.length < 2) {
                    source.sendMessage(Component.text("Usage: /hubblock remove <server>", NamedTextColor.YELLOW));
                    return;
                }
                String target = args[1];
                if (config.removeBlockedServer(target)) {
                    source.sendMessage(Component.text("Removed '" + target + "' from the blacklist.", NamedTextColor.GREEN));
                    warnIfNotSaved(source);
                } else {
                    source.sendMessage(Component.text("'" + target + "' is not in the blacklist.", NamedTextColor.RED));
                }
            }
            case "list" -> {
                List<String> list = config.getBlockedServers();
                if (list.isEmpty()) {
                    source.sendMessage(Component.text("No servers are blacklisted.", NamedTextColor.GRAY));
                } else {
                    source.sendMessage(Component.text("Blacklisted servers:", NamedTextColor.GOLD));
                    source.sendMessage(Component.text(String.join(", ", list), NamedTextColor.YELLOW));
                }
            }
            default -> source.sendMessage(Component.text("Usage: /hubblock <add|remove|list> [server]", NamedTextColor.YELLOW));
        }
    }

    private void warnIfNotSaved(CommandSource source) {
        if (!config.lastSaveSucceeded()) {
            source.sendMessage(Component.text(
                "Could not save config.yml, so this change will be lost on restart. See the proxy console.",
                NamedTextColor.RED
            ));
        }
    }

    /**
     * Also gates suggest(), so players without the permission cannot list backend servers by tab completion.
     */
    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission(config.getAdminPermission());
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();

        // Velocity passes no arguments for "/hubblock " and does not filter suggestions by what was typed.
        if (args.length <= 1) {
            return matching(SUBCOMMANDS.stream(), args.length == 0 ? "" : args[0]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("remove")) {
            return matching(config.getBlockedServers().stream(), args[1]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("add")) {
            return matching(server.getAllServers().stream()
                .map(s -> s.getServerInfo().getName())
                .filter(name -> !config.isBlocked(name)), args[1]);
        }

        return List.of();
    }

    private static List<String> matching(Stream<String> candidates, String typed) {
        String prefix = typed.toLowerCase(Locale.ROOT);
        return candidates
            .filter(candidate -> candidate.toLowerCase(Locale.ROOT).startsWith(prefix))
            .collect(Collectors.toList());
    }
}
