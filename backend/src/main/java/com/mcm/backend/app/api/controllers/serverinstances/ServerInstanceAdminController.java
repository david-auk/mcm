package com.mcm.backend.app.api.controllers.serverinstances;


import com.mcm.backend.app.api.utils.annotations.CurrentUser;
import com.mcm.backend.app.api.utils.annotations.RequireRole;
import com.mcm.backend.app.api.utils.annotations.ValidatedBody;
import com.mcm.backend.app.database.core.components.daos.DAO;
import com.mcm.backend.app.database.core.factories.DAOFactory;
import com.mcm.backend.app.database.models.server.ServerInstance;
import com.mcm.backend.app.database.models.server.ServerInstanceProperty;
import com.mcm.backend.app.database.models.server.utils.ServerCoreUtil;
import com.mcm.backend.app.database.models.users.Admin;
import com.mcm.backend.app.database.models.users.User;
import com.mcm.backend.exceptions.JsonErrorResponseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/server-instances")
public class ServerInstanceAdminController {

    @GetMapping
    @RequireRole(Admin.class)
    public ResponseEntity<List<ServerInstance>> getServerInstances(@CurrentUser User user) {
        List<ServerInstance> serverInstances;

        try (DAO<ServerInstance, UUID> serverInstanceDAO = DAOFactory.createDAO(ServerInstance.class)) {
            serverInstances = serverInstanceDAO.getAll();

            return ResponseEntity.ok(serverInstances);
        }
    }

    @GetMapping("/{id}")
    // TODO add new annotation that checks if user is at least a viewer
    public ResponseEntity<ServerInstance> getServerInstance(@PathVariable UUID id) {
        try (DAO<ServerInstance, UUID> serverInstanceDAO = DAOFactory.createDAO(ServerInstance.class)) {
            ServerInstance serverInstance = serverInstanceDAO.get(id);
            return ResponseEntity.ok(serverInstance);
        }
    }

    @PostMapping("/initialize/{id}")
    @RequireRole(Admin.class)
    public ResponseEntity<?> initializeServerInstance(@PathVariable UUID id) throws JsonErrorResponseException {
        try (DAO<ServerInstance, UUID> serverInstanceDAO = DAOFactory.createDAO(ServerInstance.class)) {
            ServerInstance serverInstance = serverInstanceDAO.get(id);

            // Check if server instance exists
            if (serverInstance == null) {
                throw new JsonErrorResponseException("Server Instance not found", HttpStatus.NOT_FOUND);
            }

            // Check if server already initialized
            if (serverInstance.getEulaAccepted()) {
                throw new JsonErrorResponseException("Already initialized", HttpStatus.CONFLICT);
            }

            List<ServerInstanceProperty> serverInstanceProperties;

            try {
                // Build serverInstance
                serverInstanceProperties = serverInstance.initialize();

                // Update the server instance
                serverInstanceDAO.update(serverInstance);

                // Store properties in DB
                try (DAO<ServerInstanceProperty, UUID> propertyDAO = DAOFactory.createDAO(ServerInstanceProperty.class)) {
                    for (ServerInstanceProperty property : serverInstanceProperties) {
                        propertyDAO.add(property);
                    }
                }

                // Return ok message
                return ResponseEntity.ok(Map.of("message", "Initialized successfully"));

            } catch ( IOException | InterruptedException e) {
                // Cleanup server files
                try {
                    ServerCoreUtil.cleanServerInstance(serverInstance);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }

                // Throw exception
                throw new JsonErrorResponseException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }

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
