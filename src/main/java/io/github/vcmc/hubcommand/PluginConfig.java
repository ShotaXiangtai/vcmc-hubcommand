package io.github.vcmc.hubcommand;

import org.slf4j.Logger;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PluginConfig {

    private static final String BLOCKED_SERVERS_KEY = "blocked-servers";

    private final Path dataDirectory;
    private final Logger logger;

    private String lobbyServerName = "lobby";
    private String alreadyInLobbyMessage = "&eYou are already in the lobby!";
    private String connectingMessage = "&aConnecting to lobby...";
    private String permission = "";
    private String adminPermission = "vcmc.hubcommand.admin";

    // Commands run on Velocity's async pool, so /hub reads this while /hubblock changes it.
    private final List<String> blockedServers = new CopyOnWriteArrayList<>();
    private volatile boolean lastSaveSucceeded = true;
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
            ConfigurationNode root = loader.load();

            lobbyServerName = root.node("lobby-server").getString("lobby");
            alreadyInLobbyMessage = root.node("messages", "already-in-lobby").getString("&eYou are already in the lobby!");
            connectingMessage = root.node("messages", "connecting").getString("&aConnecting to lobby...");
            permission = root.node("permission").getString("");
            adminPermission = root.node("admin-permission").getString("vcmc.hubcommand.admin");

            blockedServers.clear();
            ConfigurationNode blockedNode = root.node(BLOCKED_SERVERS_KEY);
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

    /**
     * Writes the blacklist back to config.yml. Configurate's YAML loader drops every comment when it saves,
     * so only the blocked-servers entry is rewritten and the rest of the file stays as the admin wrote it.
     *
     * @return false if the file was left unchanged, meaning the current list will be lost on restart
     */
    private boolean save() {
        if (configFile == null) {
            return false;
        }
        try {
            String updated = withBlockedServers(Files.readString(configFile, StandardCharsets.UTF_8));

            // An unusual layout could defeat the line-based rewrite, so never write what does not read back the same.
            List<String> readBack = YamlConfigurationLoader.builder()
                .buildAndLoadString(updated)
                .node(BLOCKED_SERVERS_KEY)
                .getList(String.class, List.of());
            if (!readBack.equals(blockedServers)) {
                logger.error("Could not update {} in config.yml automatically. Please edit it by hand: {}",
                    BLOCKED_SERVERS_KEY, blockedServers);
                return false;
            }

            Path temp = configFile.resolveSibling("config.yml.tmp");
            Files.writeString(temp, updated, StandardCharsets.UTF_8);
            Files.move(temp, configFile, StandardCopyOption.ATOMIC_MOVE);
            return true;
        } catch (IOException e) {
            logger.error("Failed to save config.yml", e);
            return false;
        }
    }

    private String withBlockedServers(String yaml) {
        String eol = yaml.contains("\r\n") ? "\r\n" : "\n";
        List<String> entry = new ArrayList<>();
        if (blockedServers.isEmpty()) {
            entry.add(BLOCKED_SERVERS_KEY + ": []");
        } else {
            entry.add(BLOCKED_SERVERS_KEY + ":");
            for (String server : blockedServers) {
                entry.add("  - " + quote(server));
            }
        }

        String[] lines = yaml.split("\\R", -1);
        List<String> result = new ArrayList<>(lines.length + entry.size());
        boolean replaced = false;
        for (int i = 0; i < lines.length; i++) {
            if (replaced || !lines[i].startsWith(BLOCKED_SERVERS_KEY + ":")) {
                result.add(lines[i]);
                continue;
            }
            result.addAll(entry);
            replaced = true;
            while (i + 1 < lines.length && continuesValue(lines[i + 1])) {
                i++;
            }
        }

        if (replaced) {
            return String.join(eol, result);
        }
        // config.yml written by a version without the blacklist has no entry yet.
        String separator = yaml.isEmpty() || yaml.endsWith("\n") ? "" : eol;
        return yaml + separator + String.join(eol, entry) + eol;
    }

    private static boolean continuesValue(String line) {
        return line.startsWith(" ") || line.startsWith("\t") || line.startsWith("-");
    }

    private static String quote(String value) {
        return '"' + value.replace("\\", "\\\\").replace("\"", "\\\"") + '"';
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

    public String getAdminPermission() {
        return adminPermission;
    }

    public List<String> getBlockedServers() {
        return new ArrayList<>(blockedServers);
    }

    public boolean isBlocked(String serverName) {
        return blockedServers.stream().anyMatch(s -> s.equalsIgnoreCase(serverName));
    }

    public synchronized boolean addBlockedServer(String serverName) {
        if (isBlocked(serverName)) {
            return false;
        }
        blockedServers.add(serverName);
        lastSaveSucceeded = save();
        return true;
    }

    public synchronized boolean removeBlockedServer(String serverName) {
        boolean removed = blockedServers.removeIf(s -> s.equalsIgnoreCase(serverName));
        if (removed) {
            lastSaveSucceeded = save();
        }
        return removed;
    }

    public boolean lastSaveSucceeded() {
        return lastSaveSucceeded;
    }
}
