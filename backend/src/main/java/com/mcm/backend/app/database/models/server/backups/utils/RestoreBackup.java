package com.mcm.backend.app.database.models.server.backups.utils;

import com.mcm.backend.app.database.core.components.Database;
import com.mcm.backend.app.database.core.components.daos.DAO;
import com.mcm.backend.app.database.core.factories.DAOFactory;
import com.mcm.backend.app.database.models.server.ServerInstance;
import com.mcm.backend.app.database.models.server.backups.Backup;
import com.mcm.backend.app.database.models.users.User;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import java.util.stream.Stream;

public class RestoreBackup {
    /**
     * Restores the given backup by copying its contents back to the server directory.
     *
     * @param backup the backup to restore
     */
    static void restoreFiles(Backup backup) {
        Path backupDir = backup.getPath();
        Path serverDir = backup.serverInstance().getPath();
        try (Stream<Path> paths = Files.walk(backupDir)) {
            paths.forEach(path -> {
                Path relative = backupDir.relativize(path);
                Path target = serverDir.resolve(relative);
                try {
                    if (Files.isDirectory(path)) {
                        Files.createDirectories(target);
                    } else {
                        Files.createDirectories(target.getParent());
                        Files.copy(path, target, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Failed to restore file: " + path, e);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException("Failed to restore backup from: " + backupDir, e);
        }
    }

    static void restoreDatabase(Backup backup) {
        try (Connection connection = Database.getConnection();
             DAO<User, UUID> userDAO = DAOFactory.createDAO(connection, User.class);
             DAO<ServerInstance, UUID> serverInstanceDAO = DAOFactory.createDAO(connection, ServerInstance.class);
             DAO<Backup, UUID> backupDAO = DAOFactory.createDAO(connection, Backup.class)) {

            // User logic:
            User user = backup.createdBy();

            if (userDAO.exists(user)) {
                userDAO.update(user);
            } else {
                userDAO.add(user);
            }

            // ServerInstance logic:
            ServerInstance serverInstance = backup.serverInstance();

            if (serverInstanceDAO.exists(serverInstance)) {
                serverInstanceDAO.update(serverInstance);
            } else {
                serverInstanceDAO.add(serverInstance);
            }

            // Backup logic:
            if (backupDAO.exists(backup)) {
                backupDAO.update(backup);
            } else {
                backupDAO.add(backup);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
