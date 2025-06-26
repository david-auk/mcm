package com.mcm.backend.app.api.controllers.serverinstances.roles;

import com.mcm.backend.app.database.core.components.daos.DAO;
import com.mcm.backend.app.database.core.components.daos.querying.QueryBuilder;
import com.mcm.backend.app.database.core.factories.DAOFactory;
import com.mcm.backend.app.database.models.logging.UserActionLog;
import com.mcm.backend.app.database.models.server.ServerInstance;
import com.mcm.backend.app.database.models.roles.RoleEntity;
import com.mcm.backend.app.database.models.users.UserRoleAssignment;
import com.mcm.backend.exceptions.JsonErrorResponseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/server-instances")
public class RoleController {
    @GetMapping("/{id}")
    // TODO add new annotation that checks if user is at least a viewer
    public ResponseEntity<ServerInstance> getServerInstance(@PathVariable UUID id) {
        try (DAO<ServerInstance, UUID> serverInstanceDAO = DAOFactory.createDAO(ServerInstance.class)) {
            ServerInstance serverInstance = serverInstanceDAO.get(id);
            return ResponseEntity.ok(serverInstance);
        }
    }
}
