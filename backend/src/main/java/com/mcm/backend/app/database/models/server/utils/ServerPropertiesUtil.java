package com.mcm.backend.app.database.models.server.utils;

import com.mcm.backend.app.database.models.server.ServerInstance;
import com.mcm.backend.app.database.models.server.ServerInstanceProperty;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.*;

/**
 * Utility for reading, writing, and initializing {@code server.properties}
 * for Minecraft server instances, using structured {@link ServerInstanceProperty}.
 */
public class ServerPropertiesUtil {

    private static final Set<String> SPECIAL_KEYS = Set.of(
            "server-port", "rcon.port", "rcon.password", "enable-rcon"
    );

    /**
     * Initializes the server.properties into a list of {@link ServerInstanceProperty}.
     * Special values are routed to the {@link ServerInstance} object instead.
     *
     * @param serverInstance the server instance
     * @return list of regular properties
     * @throws IOException if file reading fails
     */
    public static List<ServerInstanceProperty> initialize(ServerInstance serverInstance) throws IOException {
        Properties props = loadProperties(serverInstance);
        List<ServerInstanceProperty> result = new ArrayList<>();

        // Generate RCON password fresh on init
        String rconPassword = generateRandomPassword();
        String rconPort = String.valueOf(serverInstance.getPort() + 1);

        for (String key : props.stringPropertyNames()) {
            String value = props.getProperty(key);
            boolean isSpecial = SPECIAL_KEYS.contains(key);

            // Override special keys explicitly
            if (isSpecial) {
                switch (key) {
                    case "server-port" -> value = String.valueOf(serverInstance.getPort());
                    case "rcon.password" -> value = rconPassword;
                    case "rcon.port" -> value = rconPort;
                    case "enable-rcon" -> value = "true";
                    // TODO Find implementation to auto set motd from serverInstance.getDescription()
                }
            }

            result.add(new ServerInstanceProperty(
                    null, // Handle generation within constructor
                    serverInstance.getId(),
                    isSpecial,
                    inferType(value),
                    value,
                    key
            ));
        }

        // Add missing specials if they weren't in the file
        Set<String> keysInFile = new HashSet<>(props.stringPropertyNames());
        if (!keysInFile.contains("server-port")) {
            result.add(new ServerInstanceProperty(
                null,
                serverInstance.getId(),
                true,
                "integer",
                String.valueOf(serverInstance.getPort()),
                "server-port"
            ));
        }
        if (!keysInFile.contains("rcon.password")) {
            result.add(new ServerInstanceProperty(
                null,
                serverInstance.getId(),
                true,
                "string",
                rconPassword,
                "rcon.password"
            ));
        }
        if (!keysInFile.contains("rcon.port")) {
            result.add(new ServerInstanceProperty(
                null,
                serverInstance.getId(),
                true,
                "integer",
                rconPort,
                "rcon.port"
            ));
        }
        if (!keysInFile.contains("enable-rcon")) {
            result.add(new ServerInstanceProperty(
                null,
                serverInstance.getId(),
                true,
                "boolean",
                "true",
                "enable-rcon"
            ));
        }

        return result;
    }

    /**
     * Writes all given properties to server.properties
     *
     * @param instance   the server instance
     * @param properties list of properties (special + regular)
     * @throws IOException if writing fails
     */

    public static void write(ServerInstance instance, List<ServerInstanceProperty> properties) throws IOException {
        Properties props = new Properties();

        for (ServerInstanceProperty p : properties) {
            props.setProperty(p.getKey(), p.getValue());
        }

        storeProperties(getServerPropertiesPath(instance), props);
    }

    // --- Internal helper methods ---

    private static Properties loadProperties(ServerInstance instance) throws IOException {
        Path path = getServerPropertiesPath(instance);

        if (!Files.exists(path)) {
            throw new FileNotFoundException("server.properties not found for instance " + instance.getId());
        }

        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(path)) {
            props.load(in);
        }

        return props;
    }

    private static void storeProperties(Path propertiesPath, Properties props) throws IOException {
        try (OutputStream out = Files.newOutputStream(propertiesPath)) {
            props.store(out, "Generated by ServerPropertiesUtil");
        }
    }

    private static String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new SecureRandom();
        StringBuilder sb = new StringBuilder(16);

        for (int i = 0; i < 16; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }

        return sb.toString();
    }

    private static String inferType(String value) {
        if (value == null) {
            return "string";
        }

        String lower = value.toLowerCase();

        // Check boolean first
        if (lower.equals("true") || lower.equals("false")) {
            return "boolean";
        }

        // Check integer
        try {
            Integer.parseInt(value);
            return "integer";
        } catch (NumberFormatException e) {
            // Not an integer
        }

        // Default fallback
        return "string";
    }

    private static Path getServerPropertiesPath(ServerInstance serverInstance) {
        return serverInstance.getPath().resolve("server.properties");
    }
}
