package com.mcm.backend.app.database.models.server.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import com.mcm.backend.app.database.models.server.ServerInstance;
import static com.mcm.backend.constants.ServerSettings.SERVER_ROOT;

/**
 * Utility class for resolving core server-related properties, such as filesystem paths
 * and start commands, for a given {@link ServerInstance}.
 */
public class ServerCoreUtil {

    /**
     * Returns the full absolute path to the working directory of the given {@link ServerInstance}.
     * <p>
     * Assumes the server directory is located directly under {@code SERVER_ROOT} and named using
     * the instance's UUID.
     *
     * @param serverInstance the server instance for which to resolve the directory
     * @return the absolute path to the instance's working directory
     */
    public static Path getServerInstanceDirectory(ServerInstance serverInstance) {
        return Path.of(SERVER_ROOT, serverInstance.getName());
        //return String.format("%s/%s", SERVER_ROOT, serverInstance.getId());
    }

    /**
     * Constructs the shell command used to start the Minecraft server for the given {@link ServerInstance}.
     * <p>
     * Assumes the command is executed from within the instance’s server directory, and that the server JAR
     * is named {@code server.jar}. Also assumes both initial and max heap sizes are set to the value of
     * {@link ServerInstance#getAllocatedRamMB()}.
     *
     * @param serverInstance the server instance to generate the start command for
     * @return the full start command as a {@link String}, ready to be executed in a shell
     */
    public static String getServerStartCommand(ServerInstance serverInstance) {
        return "java -Xmx" + serverInstance.getAllocatedRamMB() + "M -Xms" + serverInstance.getAllocatedRamMB() + "M " +
                "-jar server.jar nogui";
    }

    /**
     * Returns the path to the default log file (latest.log) for a given {@link ServerInstance}.
     *
     * @param serverInstance the instance to resolve the log file for
     * @return absolute path to latest.log
     */
    public static Path getLogFilePath(ServerInstance serverInstance) {
        return getServerInstanceDirectory(serverInstance).resolve("latest.log");
    }

    /**
     * Validates that the server instance directory and required server.jar exist.
     *
     * @param serverInstance the server instance to validate
     * @throws IllegalStateException if the directory or JAR file is missing
     */
    public static void validateServerInstanceEnvironment(ServerInstance serverInstance) throws IllegalStateException {

        // Validate server structure

        //String dirPath = getServerInstanceDirectory(serverInstance);
        File dir = getServerInstanceDirectory(serverInstance).toFile();

        if (!dir.exists() || !dir.isDirectory()) {
            throw new IllegalStateException("Server directory does not exist: " + dir.getAbsolutePath());
        }

        File jarFile = new File(dir, "server.jar");
        if (!jarFile.exists() || !jarFile.isFile()) {
            throw new IllegalStateException("Missing server.jar in: " + dir.getAbsolutePath());
        }

        // Validate ports
        int port = serverInstance.getPort();
        int rconPort = port + 1;

        if (portInUse(port)) {
            throw new IllegalStateException("Minecraft port " + port + " is already in use.");
        }
        if (portInUse(rconPort)) {
            throw new IllegalStateException("RCON port " + rconPort + " is already in use.");
        }

    }

    /**
     * Deletes all files and folders related to the server instance.
     * This is useful for cleanup after a failed initialization or removal.
     *
     * @param instance the instance to clean
     * @throws IOException if deletion fails
     */
    public static void cleanServerInstance(ServerInstance instance) throws IOException {
        Path instanceDir = getServerInstanceDirectory(instance);

        if (!Files.exists(instanceDir)) return; // nothing to delete

        Files.walkFileTree(instanceDir, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    // -- Helper methods --

    private static boolean portInUse(int port) {
        try (java.net.ServerSocket socket = new java.net.ServerSocket(port)) {
            socket.setReuseAddress(true);
            return false;
        } catch (IOException e) {
            return true;
        }
    }
}
