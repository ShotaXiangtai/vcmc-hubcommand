package io.github.vcmc.hubcommand;

import org.slf4j.Logger;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class PluginConfig {

    private final Path dataDirectory;
    private final Logger logger;

    private String lobbyServerName = "lobby";
    private String alreadyInLobbyMessage = "&eYou are already in the lobby!";
    private String connectingMessage = "&aConnecting to lobby...";
    private String permission = "";

    public PluginConfig(Path dataDirectory, Logger logger) {
        this.dataDirectory = dataDirectory;
        this.logger = logger;
    }

    public void load() {
        Path configFile = dataDirectory.resolve("config.yml");

        if (!Files.exists(configFile)) {
            try {
                Files.createDirectories(dataDirectory);
                try (InputStream in = getClass().getResourceAsStream("/config.yml")) {
                    if (in != null) {
                        Files.copy(in, configFile);
                    }
                }
            } catch (IOException e) {
                logger.error("Failed to create default config.yml", e);
                return;
            }
        }

        try {
            YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                .path(configFile)
                .build();
            ConfigurationNode root = loader.load();

            lobbyServerName = root.node("lobby-server").getString("lobby");
            alreadyInLobbyMessage = root.node("messages", "already-in-lobby").getString("&eYou are already in the lobby!");
            connectingMessage = root.node("messages", "connecting").getString("&aConnecting to lobby...");
            permission = root.node("permission").getString("");
        } catch (IOException e) {
            logger.error("Failed to load config.yml", e);
        }
    }

    public String getLobbyServerName() {
        return lobbyServerName;
    }

    public String getAlreadyInLobbyMessage() {
        return alreadyInLobbyMessage;
    }

    public String getConnectingMessage() {
        return connectingMessage;
    }

    public String getPermission() {
        return permission;
    }
}
