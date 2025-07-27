package com.mcm.backend.app.api.controllers.serverinstances.backup;

import com.mcm.backend.app.api.utils.LoggingUtil;
import com.mcm.backend.app.api.utils.annotations.CurrentUser;
import com.mcm.backend.app.api.utils.annotations.RequireServerInstanceRole;
import com.mcm.backend.app.database.core.components.Database;
import com.mcm.backend.app.database.core.components.daos.DAO;
import com.mcm.backend.app.database.core.components.daos.querying.QueryBuilder;
import com.mcm.backend.app.database.core.factories.DAOFactory;
import com.mcm.backend.app.database.models.logging.ActionType;
import com.mcm.backend.app.database.models.roles.Role;
import com.mcm.backend.app.database.models.server.ServerInstance;
import com.mcm.backend.app.database.models.server.backups.Backup;
import com.mcm.backend.app.database.models.users.User;
import com.mcm.backend.exceptions.JsonErrorResponseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/server-instances/{serverInstanceId}/backups")
public class BackupController {
    @GetMapping
    @RequireServerInstanceRole(Role.MAINTAINER)
    public ResponseEntity<List<Backup>> getBackups(@PathVariable UUID serverInstanceId) throws JsonErrorResponseException, NoSuchFieldException {
        try (DAO<ServerInstance, UUID> serverInstanceDAO = DAOFactory.createDAO(ServerInstance.class)) {
            ServerInstance serverInstance = serverInstanceDAO.get(serverInstanceId);

            if (serverInstance == null) {
                throw new JsonErrorResponseException("Server instance not found", HttpStatus.NOT_FOUND);
            }

            try (DAO<Backup, UUID> backupDAO = DAOFactory.createDAO(Backup.class)) {
                List<Backup> backups = new QueryBuilder<>(backupDAO)
                        .where(Backup.class.getDeclaredField("serverInstance"), serverInstanceId)
                        .get();

                return ResponseEntity.ok(backups);
            }
        }
    }

    @PostMapping
    @RequireServerInstanceRole(Role.MAINTAINER)
    public ResponseEntity<?> createBackup(@PathVariable UUID serverInstanceId, @CurrentUser User user) throws JsonErrorResponseException, NoSuchFieldException {
        try (DAO<ServerInstance, UUID> serverInstanceDAO = DAOFactory.createDAO(ServerInstance.class)) {
            ServerInstance serverInstance = serverInstanceDAO.get(serverInstanceId);
            if (serverInstance == null) {
                throw new JsonErrorResponseException("Server instance not found", HttpStatus.NOT_FOUND);
            }

            Backup backup = new Backup(serverInstance, user);

            // Create new backup
            backup.write();

            try (DAO<Backup, UUID> backupDAO = DAOFactory.createDAO(Backup.class)) {
                backupDAO.add(backup);

                return ResponseEntity.ok(backup);
            }
        }
    }

    @PostMapping("/restore/{backupId}")
    @RequireServerInstanceRole(Role.MAINTAINER)
    public ResponseEntity<?> restoreBackup(@PathVariable UUID serverInstanceId, @PathVariable UUID backupId) throws JsonErrorResponseException, NoSuchFieldException {
        try (Connection connection = Database.getConnection();
             DAO<ServerInstance, UUID> serverInstanceDAO = DAOFactory.createDAO(connection, ServerInstance.class)) {

            ServerInstance serverInstance = serverInstanceDAO.get(serverInstanceId);
            if (serverInstance == null) {
                throw new JsonErrorResponseException("Server instance not found", HttpStatus.NOT_FOUND);
            }

            Backup backup;

            try (DAO<Backup, UUID> backupDAO = DAOFactory.createDAO(connection, Backup.class)) {
                backup = backupDAO.get(backupId);
                if (backup == null) {
                    throw new JsonErrorResponseException("Backup not found", HttpStatus.NOT_FOUND);
                }
            }

            // Create new backup
            backup.restore();

            // TODO Log
            // LoggingUtil.log(ActionType.Bac);

            return ResponseEntity.ok(backup);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
