package com.mcm.backend.app.api.controllers.serverinstances.operational;


import com.mcm.backend.app.api.utils.LoggingUtil;
import com.mcm.backend.app.api.utils.annotations.CurrentUser;
import com.mcm.backend.app.api.utils.annotations.RequireServerInstanceRole;
import com.mcm.backend.app.database.core.components.daos.DAO;
import com.mcm.backend.app.database.core.factories.DAOFactory;
import com.mcm.backend.app.database.models.logging.ActionType;
import com.mcm.backend.app.database.models.roles.Role;
import com.mcm.backend.app.database.models.server.ServerInstance;
import com.mcm.backend.app.database.models.users.User;
import com.mcm.backend.exceptions.JsonErrorResponseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/server-instances/{id}/stop")
public class StopController {

    @PostMapping
    @RequireServerInstanceRole(Role.OPERATOR)
    public ResponseEntity<?> stopServer(@CurrentUser User currentUser, @PathVariable UUID id) throws JsonErrorResponseException {
        try (DAO<ServerInstance, UUID> serverInstanceDAO = DAOFactory.createDAO(ServerInstance.class)) {

            ServerInstance serverInstance = serverInstanceDAO.get(id);

            if (serverInstance == null) {
                throw new JsonErrorResponseException("Server Instance not found", HttpStatus.NOT_FOUND);
            }

            if (!serverInstance.isRunning()) {
                throw new JsonErrorResponseException("Server Instance not running", HttpStatus.CONFLICT);
            }

            // Stop the server
            serverInstance.stop();

            // Log the action
            LoggingUtil.log(ActionType.STOP_SERVER, currentUser, serverInstance);

            // Return OK signal
            return ResponseEntity.ok().build();
        }
    }
}
