package com.mcm.backend.app.database.models.server.utils;

import com.mcm.backend.app.api.utils.process.LogEntry;
import com.mcm.backend.app.api.utils.process.ProcessStatus;
import com.mcm.backend.app.database.models.server.ServerInstance;
import com.mcm.backend.app.database.models.server.ServerInstanceProperty;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.util.List;

/**
 * Utility for fully initializing a Minecraft {@link ServerInstance}.
 */
public class ServerInitializerUtil {

    /**
     * Initializes the server instance directory and runtime files.
     * Runs server once to generate EULA, accepts it, waits for properties,
     * then returns initialized properties (including special keys).
     *
     * @param instance the server instance to initialize
     * @return list of initialized {@link ServerInstanceProperty} including specials
     * @throws IOException           if file I/O fails
     * @throws IllegalStateException if expected steps fail
     * @throws InterruptedException  if subprocess is interrupted
     */
    public static List<ServerInstanceProperty> initialize(ServerInstance instance, ProcessStatus ps)
    throws IOException, InterruptedException {
        Path serverDir = instance.getPath();
        Path jarPath = serverDir.resolve("server.jar");

        // Step 1: Create the server directory
        ps.getLogs().add(new LogEntry("Creating server directory"));
        Files.createDirectory(serverDir);

        // Step 2: Download the server JAR
        ps.getLogs().add(new LogEntry("Downloading server JAR from: " + instance.getJarUrl()));
        downloadServerJar(instance.getJarUrl(), jarPath);

        // Step 3: Run server until eula.txt exists (max 60s)
        ps.getLogs().add(new LogEntry("Running server to generate eula.txt (This could take a while...)"));
        runUntilFileExists(serverDir, "eula.txt", 60_000);

        // Step 4: Accept EULA by modifying eula.txt
        ps.getLogs().add(new LogEntry("Accepting EULA"));
        acceptEula(serverDir);

        // Step 5: Wait for server.properties to appear (max 60s)
        Path propsPath = serverDir.resolve("server.properties");
        if (!Files.exists(propsPath)) {
            ps.getLogs().add(new LogEntry("Waiting for server.properties"));
            runUntilFileExists(serverDir, "server.properties", 60_000);
            ps.getLogs().add(new LogEntry("server.properties generated"));
        }

        // Step 6: Read, enrich and write back server.properties with specials
        ps.getLogs().add(new LogEntry("Initializing properties"));
        List<ServerInstanceProperty> initializedProperties = ServerPropertiesUtil.initialize(instance);
        ServerPropertiesUtil.write(instance, initializedProperties);

        // Step 7: Mark EULA accepted in memory
        instance.setEulaAccepted(true);

        return initializedProperties;
    }

    private static void downloadServerJar(String url, Path destination) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestProperty("User-Agent", "Minecraft-Server-Initializer");

        try (InputStream in = connection.getInputStream();
             OutputStream out = Files.newOutputStream(destination, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            in.transferTo(out);
        }

        if (!Files.exists(destination)) {
            throw new IOException("Failed to download server JAR.");
        }
    }

    private static void runUntilFileExists(Path serverDir, String expectedFilename, long timeoutMillis) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("java", "-jar", "server.jar", "nogui");
        pb.directory(serverDir.toFile());
        pb.redirectOutput(serverDir.resolve("latest.log").toFile());
        pb.redirectErrorStream(true);

        Process process = pb.start();

        Path targetFile = serverDir.resolve(expectedFilename);
        long start = System.currentTimeMillis();

        while (!Files.exists(targetFile)) {
            if (System.currentTimeMillis() - start > timeoutMillis) {
                process.destroy();
                throw new IllegalStateException("Timeout waiting for file: " + expectedFilename);
            }
            Thread.sleep(500); // Check every 0.5s
        }

        process.destroy(); // Once file exists, cleanly terminate
    }

    private static void acceptEula(Path serverDir) throws IOException {
        Path eulaPath = serverDir.resolve("eula.txt");

        if (!Files.exists(eulaPath)) {
            throw new IllegalStateException("eula.txt not generated after first run");
        }

        String content = Files.readString(eulaPath);
        String modified = content.replace("eula=false", "eula=true");
        Files.writeString(eulaPath, modified, StandardOpenOption.TRUNCATE_EXISTING);
    }
}
