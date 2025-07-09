package com.mcm.backend.app.api.controllers.serverinstances.backup;

import com.mcm.backend.app.api.utils.annotations.CurrentUser;
import com.mcm.backend.app.api.utils.annotations.RequireRole;
import com.mcm.backend.app.api.utils.annotations.RequireServerInstanceRole;
import com.mcm.backend.app.database.core.components.daos.DAO;
import com.mcm.backend.app.database.core.components.daos.querying.QueryBuilder;
import com.mcm.backend.app.database.core.factories.DAOFactory;
import com.mcm.backend.app.database.models.roles.Role;
import com.mcm.backend.app.database.models.server.ServerInstance;
import com.mcm.backend.app.database.models.server.backups.Backup;
import com.mcm.backend.app.database.models.server.backups.utils.CreateBackup;
import com.mcm.backend.app.database.models.users.Admin;
import com.mcm.backend.app.database.models.users.User;
import com.mcm.backend.exceptions.JsonErrorResponseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
                        .where(Backup.class.getDeclaredField("serverInstanceId"), serverInstanceId)
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

            Backup backup = CreateBackup.createBackup(serverInstance, user);

            try (DAO<Backup, UUID> backupDAO = DAOFactory.createDAO(Backup.class)) {
                backupDAO.add(backup);

                return ResponseEntity.ok(backup);
            }
        }
    }
}
