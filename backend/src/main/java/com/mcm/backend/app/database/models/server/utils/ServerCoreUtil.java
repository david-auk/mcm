package com.mcm.backend.app.database.models.server.utils;

import java.io.File;

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
    public static String getServerInstanceDirectory(ServerInstance serverInstance) {
        return String.format("%s/%s", SERVER_ROOT, serverInstance.getId());
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
    public static String getLogFilePath(ServerInstance serverInstance) {
        return getServerInstanceDirectory(serverInstance) + "/latest.log";
    }


    /**
     * Validates that the server instance directory and required server.jar exist.
     *
     * @param serverInstance the server instance to validate
     * @throws IllegalStateException if the directory or JAR file is missing
     */
    public static void validateServerInstanceEnvironment(ServerInstance serverInstance) throws IllegalStateException {
        String dirPath = getServerInstanceDirectory(serverInstance);
        File dir = new File(dirPath);

        if (!dir.exists() || !dir.isDirectory()) {
            throw new IllegalStateException("Server directory does not exist: " + dirPath);
        }

        File jarFile = new File(dir, "server.jar");
        if (!jarFile.exists() || !jarFile.isFile()) {
            throw new IllegalStateException("Missing server.jar in: " + dirPath);
        }
    }

}
