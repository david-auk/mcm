package com.mcm.backend.app.database.models.server.backups.utils;

import com.mcm.backend.app.database.models.server.ServerInstance;
import com.mcm.backend.app.database.models.server.backups.Backup;
import com.mcm.backend.app.database.models.server.utils.ServerCoreUtil;
import com.mcm.backend.app.database.models.users.User;

import java.nio.file.Path;

public class CreateBackup {

    public static Backup createBackup(ServerInstance serverInstance, User user) {
        Backup backup = new Backup(
                null, // Generate new id
                serverInstance.getId(),
                user.getId(),
                null // Use current date
        );

        // Write all server files
        writeServerFiles(backup, serverInstance, user);

        // Write additional metadata describing the current server config.
        writeMetadata(backup, serverInstance, user);

        return backup;
    }

    private static void writeServerFiles(Backup backup, ServerInstance serverInstance, User user) {
        Path serverDir = serverInstance.getPath();
    }

    private static void writeMetadata(Backup backup, ServerInstance serverInstance, User user) {
        Path metadataFile = serverInstance.getPath();
    }

}
