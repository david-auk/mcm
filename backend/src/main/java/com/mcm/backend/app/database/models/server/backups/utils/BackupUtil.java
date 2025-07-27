package com.mcm.backend.app.database.models.server.backups.utils;

import com.mcm.backend.app.database.models.server.ServerInstance;
import com.mcm.backend.app.database.models.server.backups.Backup;

import java.nio.file.Path;
import java.util.UUID;

public class BackupUtil {

    public static void write(Backup backup) {

        ServerInstance serverInstance = backup.serverInstance();

        if (serverInstance.isRunning()) {
            serverInstance.sendCommand("say Backing up server...");
        }

        // Write all server files
        WriteBackup.writeServerFiles(backup);

        // Write additional metadata describing the current server config.
        WriteBackup.writeMetadata(backup, getMetadataFile(backup));

        if (serverInstance.isRunning()) {
            serverInstance.sendCommand("say Backup complete.");
        }
    }

    public static Backup getLatest(Path serverInstancePath) {
        return LoadBackup.getLatestBackup(serverInstancePath);
    }

    public static void restore(Backup backup) {

        if (backup.serverInstance().isRunning()) {
            throw new IllegalStateException("Server instance cant run whilst restoring");
        }

        // Restore files
        RestoreBackup.restoreFiles(backup);

        // Restore data
        RestoreBackup.restoreDatabase(backup);
    }

    static Path getMetadataFile(Backup backup) {
        return getMetadataFile(backup.getPath());
    }

    static Path getMetadataFile(Path serverInstancePath, UUID backupInstanceId) {
        Path backupInstancePath = Backup.getPath(serverInstancePath, backupInstanceId);
        return getMetadataFile(backupInstancePath);
    }

    static Path getMetadataFile(Path root) {
        return root.resolve("metadata.json");
    }

}
