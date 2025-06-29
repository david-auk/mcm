package com.mcm.backend.app.api.controllers.serverinstances.roles.viewer;

import com.mcm.backend.app.api.utils.annotations.RequireServerInstanceRole;
import com.mcm.backend.app.database.core.components.daos.DAO;
import com.mcm.backend.app.database.core.factories.DAOFactory;
import com.mcm.backend.app.database.models.roles.Role;
import com.mcm.backend.app.database.models.server.ServerInstance;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/server-instances")
public class ViewerController {
    @GetMapping("/{id}")
    @RequireServerInstanceRole(Role.VIEWER)
    public ResponseEntity<ServerInstance> getServerInstance(@PathVariable UUID id) {
        try (DAO<ServerInstance, UUID> serverInstanceDAO = DAOFactory.createDAO(ServerInstance.class)) {
            ServerInstance serverInstance = serverInstanceDAO.get(id);
            return ResponseEntity.ok(serverInstance);
        }
    }
}
