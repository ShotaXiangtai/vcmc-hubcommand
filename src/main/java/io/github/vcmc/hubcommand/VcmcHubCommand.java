package io.github.vcmc.hubcommand;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
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

    @Inject
    public VcmcHubCommand(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        PluginConfig config = new PluginConfig(dataDirectory, logger);
        config.load();

        CommandManager commandManager = server.getCommandManager();
        CommandMeta meta = commandManager.metaBuilder("hub")
            .aliases("lobby", "l")
            .plugin(this)
            .build();

        commandManager.register(meta, new HubCommand(server, config));
        logger.info("vcmc-hubcommand enabled. Lobby server: {}", config.getLobbyServerName());
    }
}
