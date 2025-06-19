package com.mcm.backend.app.database.models.server.utils;

import com.mcm.backend.app.database.models.server.ServerInstance;
import com.mcm.backend.app.database.models.server.utils.ServerCoreUtil;

import java.io.IOException;
import java.util.UUID;

/**
 * Utility class for managing Minecraft server processes using tmux sessions.
 * Each {@link ServerInstance} is managed in a dedicated tmux session, named by its UUID.
 */
public class TmuxUtil {

    /**
     * Checks if a tmux session is currently running for the given {@link ServerInstance}.
     *
     * @param instance the server instance to check
     * @return {@code true} if the tmux session exists, {@code false} otherwise
     */
    public static boolean isTmuxSessionRunning(ServerInstance instance) {
        UUID sessionId = instance.getId();
        try {
            ProcessBuilder pb = new ProcessBuilder("tmux", "has-session", "-t", sessionId.toString());
            Process process = pb.start();
            return process.waitFor() == 0;
        } catch (Exception e) {
            System.err.println("Error checking tmux session: " + e.getMessage());
            return false;
        }
    }

    /**
     * Starts a new tmux session for the given {@link ServerInstance}, launching the server inside the session.
     * Output is redirected to a log file. Throws an exception on failure.
     *
     * @param instance the server instance to start
     * @throws IllegalStateException if the server is already running or the tmux start fails
     */
    public static void startServerInstance(ServerInstance instance) throws IllegalStateException {
        if (isTmuxSessionRunning(instance)) {
            throw new IllegalStateException("Server is already running for instance " + instance.getId());
        }

        // Validate that environment is ready
        ServerCoreUtil.validateServerInstanceEnvironment(instance);

        String sessionName = instance.getId().toString();
        String workingDir = ServerCoreUtil.getServerInstanceDirectory(instance);
        String startCommand = ServerCoreUtil.getServerStartCommand(instance);
        String fullCommand = String.format("cd %s && %s >> latest.log 2>&1", workingDir, startCommand);

        try {
            ProcessBuilder pb = new ProcessBuilder("tmux", "new-session", "-d", "-s", sessionName, fullCommand);
            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new IllegalStateException("tmux failed to start session. Exit code: " + exitCode);
            }

        } catch (Exception e) {
            throw new IllegalStateException("Failed to start server instance: " + e.getMessage(), e);
        }
    }



    /**
     * Stops the tmux session associated with the given {@link ServerInstance}.
     * <p>
     * Throws an exception if the server is not running or if tmux fails to stop the session.
     *
     * @param instance the server instance to stop
     * @throws IllegalStateException if the session is not running or cannot be stopped
     */
    public static void stopServerInstance(ServerInstance instance) {
        if (!isTmuxSessionRunning(instance)) {
            throw new IllegalStateException("Server is not currently running for instance " + instance.getId());
        }

        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "tmux", "kill-session", "-t", instance.getId().toString()
            );
            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new IllegalStateException("Failed to stop tmux session. Exit code: " + exitCode);
            }

        } catch (IOException | InterruptedException e) {
            throw new IllegalStateException("Failed to stop server instance: " + e.getMessage(), e);
        }
    }
}
