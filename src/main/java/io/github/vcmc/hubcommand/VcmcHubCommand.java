package io.github.vcmc.hubcommand;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(
    id = "vcmc-hubcommand",
    name = "VCMC Hub Command",
    version = "1.0.0",
    description = "Provides /hub, /lobby, /l commands to connect to the lobby server",
    authors = {"vcmc"}
)
public class VcmcHubCommand {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private PluginConfig config;

    @Inject
    public VcmcHubCommand(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        config = new PluginConfig(dataDirectory, logger);
        config.load();

        CommandManager commandManager = server.getCommandManager();

        // /hub, /lobby, /l
        CommandMeta hubMeta = commandManager.metaBuilder("hub")
            .aliases("lobby", "l")
            .plugin(this)
            .build();
        commandManager.register(hubMeta, new HubCommand(server, config));

        // /hubblock
        CommandMeta blockMeta = commandManager.metaBuilder("hubblock")
            .plugin(this)
            .build();
        commandManager.register(blockMeta, new BlacklistCommand(server, config));

        logger.info("vcmc-hubcommand enabled. Lobby server: {}", config.getLobbyServerName());
    }

    @Subscribe
    public void onCommandExecute(CommandExecuteEvent event) {
        if (!(event.getCommandSource() instanceof Player player)) {
            return;
        }

        if (config == null) {
            return;
        }

        String command = event.getCommand().split(" ")[0].toLowerCase();
        if (!command.equals("hub") && !command.equals("lobby") && !command.equals("l")) {
            return;
        }

        if (player.getCurrentServer().isPresent()) {
            String currentName = player.getCurrentServer().get().getServerInfo().getName();
            if (config.isBlocked(currentName)) {
                event.setResult(CommandExecuteEvent.CommandResult.forwardToServer());
            }
        }
    }
}
