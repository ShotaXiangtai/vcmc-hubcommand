package io.github.vcmc.hubcommand;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;
import java.util.stream.Collectors;

public class BlacklistCommand implements SimpleCommand {

    private final ProxyServer server;
    private final PluginConfig config;

    public BlacklistCommand(ProxyServer server, PluginConfig config) {
        this.server = server;
        this.config = config;
    }

    @Override
    public void execute(Invocation invocation) {
        if (!(invocation.source() instanceof Player player)) {
            invocation.source().sendMessage(Component.text("This command can only be used by players.", NamedTextColor.RED));
            return;
        }

        if (!player.hasPermission(config.getAdminPermission())) {
            player.sendMessage(Component.text("You don't have permission to use this command.", NamedTextColor.RED));
            return;
        }

        String[] args = invocation.arguments();

        if (args.length < 1) {
            player.sendMessage(Component.text("Usage: /hubblock <add|remove|list> [server]", NamedTextColor.YELLOW));
            return;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "add" -> {
                if (args.length < 2) {
                    player.sendMessage(Component.text("Usage: /hubblock add <server>", NamedTextColor.YELLOW));
                    return;
                }
                String target = args[1];
                if (config.addBlockedServer(target)) {
                    player.sendMessage(Component.text("Added '" + target + "' to the blacklist.", NamedTextColor.GREEN));
                } else {
                    player.sendMessage(Component.text("'" + target + "' is already in the blacklist.", NamedTextColor.RED));
                }
            }
            case "remove" -> {
                if (args.length < 2) {
                    player.sendMessage(Component.text("Usage: /hubblock remove <server>", NamedTextColor.YELLOW));
                    return;
                }
                String target = args[1];
                if (config.removeBlockedServer(target)) {
                    player.sendMessage(Component.text("Removed '" + target + "' from the blacklist.", NamedTextColor.GREEN));
                } else {
                    player.sendMessage(Component.text("'" + target + "' is not in the blacklist.", NamedTextColor.RED));
                }
            }
            case "list" -> {
                List<String> list = config.getBlockedServers();
                if (list.isEmpty()) {
                    player.sendMessage(Component.text("No servers are blacklisted.", NamedTextColor.GRAY));
                } else {
                    player.sendMessage(Component.text("Blacklisted servers:", NamedTextColor.GOLD));
                    player.sendMessage(Component.text(String.join(", ", list), NamedTextColor.YELLOW));
                }
            }
            default -> player.sendMessage(Component.text("Usage: /hubblock <add|remove|list> [server]", NamedTextColor.YELLOW));
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        if (args.length == 0) {
            return List.of();
        }

        if (args.length == 1) {
            return List.of("add", "remove", "list");
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("remove")) {
            return config.getBlockedServers();
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("add")) {
            return server.getAllServers().stream()
                .map(s -> s.getServerInfo().getName())
                .filter(name -> !config.isBlocked(name))
                .collect(Collectors.toList());
        }

        return List.of();
    }
}
