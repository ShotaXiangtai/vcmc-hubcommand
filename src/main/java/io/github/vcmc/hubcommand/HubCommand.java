package io.github.vcmc.hubcommand;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.Optional;

public class HubCommand implements SimpleCommand {

    private final ProxyServer server;
    private final PluginConfig config;

    public HubCommand(ProxyServer server, PluginConfig config) {
        this.server = server;
        this.config = config;
    }

    @Override
    public void execute(Invocation invocation) {
        if (!(invocation.source() instanceof Player player)) {
            invocation.source().sendMessage(Component.text("This command can only be used by players.", NamedTextColor.RED));
            return;
        }

        String lobbyName = config.getLobbyServerName();
        Optional<RegisteredServer> lobbyServer = server.getServer(lobbyName);

        if (lobbyServer.isEmpty()) {
            player.sendMessage(Component.text(
                "Lobby server '" + lobbyName + "' not found. Check proxy configuration.",
                NamedTextColor.RED
            ));
            return;
        }

        if (player.getCurrentServer().isPresent() &&
            player.getCurrentServer().get().getServerInfo().getName().equalsIgnoreCase(lobbyName)) {
            player.sendMessage(deserialize(config.getAlreadyInLobbyMessage()));
            return;
        }

        player.sendMessage(deserialize(config.getConnectingMessage()));
        player.createConnectionRequest(lobbyServer.get()).fireAndForget();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        String permission = config.getPermission();
        if (permission == null || permission.isEmpty()) {
            return true;
        }
        return invocation.source().hasPermission(permission);
    }

    private Component deserialize(String text) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(text);
    }
}
