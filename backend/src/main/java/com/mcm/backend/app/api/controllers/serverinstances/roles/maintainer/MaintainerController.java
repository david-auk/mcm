package com.mcm.backend.app.api.controllers.serverinstances.roles.maintainer;

import com.mcm.backend.app.api.utils.LoggingUtil;
import com.mcm.backend.app.api.utils.annotations.CurrentUser;
import com.mcm.backend.app.api.utils.annotations.RequireServerInstanceRole;
import com.mcm.backend.app.api.utils.annotations.ValidatedBody;
import com.mcm.backend.app.database.core.components.daos.DAO;
import com.mcm.backend.app.database.core.factories.DAOFactory;
import com.mcm.backend.app.database.models.roles.Role;
import com.mcm.backend.app.database.models.server.ServerInstance;
import com.mcm.backend.app.database.models.users.User;
import com.mcm.backend.exceptions.JsonErrorResponseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/server-instances")
public class MaintainerController {

    @PutMapping("/{id}")
    @RequireServerInstanceRole(Role.MAINTAINER)
    public ResponseEntity<ServerInstance> putServerInstance(@CurrentUser User currentUser, @PathVariable UUID id, @ValidatedBody(ServerInstance.class) ServerInstance serverInstance) throws JsonErrorResponseException, NoSuchFieldException {

        if (!serverInstance.getId().equals(id)) {
            throw new JsonErrorResponseException("Server Instance ID does not match path variable", HttpStatus.BAD_REQUEST);
        }

        try (DAO<ServerInstance, UUID> serverInstanceDAO = DAOFactory.createDAO(ServerInstance.class)) {
            ServerInstance oldServerInstance = serverInstanceDAO.get(id);

            if (oldServerInstance == null) {
                throw new JsonErrorResponseException("Server Instance not found", HttpStatus.NOT_FOUND);
            }

            // If the servername changed
            if (!oldServerInstance.getName().equals(serverInstance.getName())) {
                if (serverInstanceDAO.existsByUniqueField(
                        ServerInstance.class.getDeclaredField("name"),
                        serverInstance.getName()
                )) {
                    throw new JsonErrorResponseException("Server Instance name already exists", HttpStatus.CONFLICT);
                }
            }

            // If the server is already initialized
            if (oldServerInstance.getEulaAccepted()) {

                // Validate URL Change
                String oldUrl = oldServerInstance.getJarUrl();
                String newUrl = serverInstance.getJarUrl();

                // If URL changed
                if (!oldUrl.equals(newUrl)) {
                    throw new JsonErrorResponseException("Cannot change URL after server has been initialized", HttpStatus.CONFLICT);
                }
            }

            // Make sure the user accepts the EULA
            if (!serverInstance.getEulaAccepted()) {
                throw new JsonErrorResponseException("Please accept the Minecraft EULA", HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS);
            }

            // Make sure the user can't set the initialized flag via the UI.
            // Only the initializer flow can do this...
            serverInstance.setEulaAccepted(oldServerInstance.getEulaAccepted());

            // TODO Add logging
            //LoggingUtil.log(ActionType.);

            // Update the database
            serverInstanceDAO.update(serverInstance);

            return ResponseEntity.ok(serverInstance);
        }
    }

    @DeleteMapping("/{id}")
    @RequireServerInstanceRole(Role.MAINTAINER)
    public ResponseEntity<?> deleteServerInstance(@CurrentUser User currentUser, @PathVariable UUID id) throws JsonErrorResponseException {
        try (DAO<ServerInstance, UUID> serverInstanceDAO = DAOFactory.createDAO(ServerInstance.class)) {
            if (!serverInstanceDAO.existsByPrimaryKey(id)) {
                throw new JsonErrorResponseException("Server Instance not found", HttpStatus.NOT_FOUND);
            }

            //LoggingUtil.log(ActionType.DELETE_SERVER);

            serverInstanceDAO.delete(id);

            return ResponseEntity.ok("Server Instance deleted");
        }
    }
}
