package com.mcm.backend.app.api.controllers.serverinstances;


import com.mcm.backend.app.api.utils.annotations.CurrentUser;
import com.mcm.backend.app.api.utils.annotations.RequireRole;
import com.mcm.backend.app.api.utils.annotations.ValidatedBody;
import com.mcm.backend.app.database.core.components.daos.DAO;
import com.mcm.backend.app.database.core.factories.DAOFactory;
import com.mcm.backend.app.database.models.server.ServerInstance;
import com.mcm.backend.app.database.models.users.Admin;
import com.mcm.backend.app.database.models.users.User;
import com.mcm.backend.exceptions.JsonErrorResponseException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/server-instances")
public class ServerInstanceAdminController {

    @GetMapping
    @RequireRole(Admin.class)
    public ResponseEntity<List<ServerInstance>> getServerInstances() {
        List<ServerInstance> serverInstances;

        try (DAO<ServerInstance, UUID> serverInstanceDAO = DAOFactory.createDAO(ServerInstance.class)) {
            serverInstances = serverInstanceDAO.getAll();

            return ResponseEntity.ok(serverInstances);
        }
    }

    @PostMapping
    @RequireRole(Admin.class)
    public ResponseEntity<?> createServerInstance(@ValidatedBody(ServerInstance.class) ServerInstance serverInstance, @CurrentUser User user) throws JsonErrorResponseException {

        serverInstance.setEulaAccepted(false);

        try (DAO<ServerInstance, UUID> serverInstanceDAO = DAOFactory.createDAO(ServerInstance.class)) {

            // TODO Check name in use

            serverInstanceDAO.add(serverInstance);

            return ResponseEntity.ok().build();

        }
    }
}
