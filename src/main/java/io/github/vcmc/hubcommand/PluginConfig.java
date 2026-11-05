package io.github.vcmc.hubcommand;

import org.slf4j.Logger;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class PluginConfig {

    private final Path dataDirectory;
    private final Logger logger;

    private String lobbyServerName = "lobby";
    private String alreadyInLobbyMessage = "&eYou are already in the lobby!";
    private String connectingMessage = "&aConnecting to lobby...";
    private String blockedMessage = "&cYou cannot use this command on this server.";
    private String permission = "";
    private String adminPermission = "vcmc.hubcommand.admin";

    private final List<String> blockedServers = new ArrayList<>();
    private ConfigurationNode rootNode;
    private Path configFile;

    public PluginConfig(Path dataDirectory, Logger logger) {
        this.dataDirectory = dataDirectory;
        this.logger = logger;
    }

    public void load() {
        configFile = dataDirectory.resolve("config.yml");

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
            rootNode = loader.load();

            lobbyServerName = rootNode.node("lobby-server").getString("lobby");
            alreadyInLobbyMessage = rootNode.node("messages", "already-in-lobby").getString("&eYou are already in the lobby!");
            connectingMessage = rootNode.node("messages", "connecting").getString("&aConnecting to lobby...");
            blockedMessage = rootNode.node("messages", "blocked").getString("&cYou cannot use this command on this server.");
            permission = rootNode.node("permission").getString("");
            adminPermission = rootNode.node("admin-permission").getString("vcmc.hubcommand.admin");

            blockedServers.clear();
            ConfigurationNode blockedNode = rootNode.node("blocked-servers");
            if (!blockedNode.virtual() && blockedNode.isList()) {
                for (ConfigurationNode node : blockedNode.childrenList()) {
                    String s = node.getString();
                    if (s != null && !s.isEmpty()) {
                        blockedServers.add(s);
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Failed to load config.yml", e);
        }
    }

    public void save() {
        try {
            if (rootNode == null || configFile == null) {
                return;
            }
            rootNode.node("blocked-servers").set(blockedServers);
            YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                .path(configFile)
                .build();
            loader.save(rootNode);
        } catch (IOException e) {
            logger.error("Failed to save config.yml", e);
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

    public String getBlockedMessage() {
        return blockedMessage;
    }

    public String getPermission() {
        return permission;
    }

    public String getAdminPermission() {
        return adminPermission;
    }

    public List<String> getBlockedServers() {
        return new ArrayList<>(blockedServers);
    }

    public boolean isBlocked(String serverName) {
        return blockedServers.stream().anyMatch(s -> s.equalsIgnoreCase(serverName));
    }

    public boolean addBlockedServer(String serverName) {
        if (isBlocked(serverName)) {
            return false;
        }
        blockedServers.add(serverName);
        save();
        return true;
    }

    public boolean removeBlockedServer(String serverName) {
        boolean removed = blockedServers.removeIf(s -> s.equalsIgnoreCase(serverName));
        if (removed) {
            save();
        }
        return removed;
    }
}
