package com.mcm.backend.app.api.controllers.serverinstances.propperties;

import com.mcm.backend.app.api.utils.LoggingUtil;
import com.mcm.backend.app.api.utils.annotations.CurrentUser;
import com.mcm.backend.app.api.utils.annotations.RequireServerInstanceRole;
import com.mcm.backend.app.api.utils.requestbody.RequestBodyUtil;
import com.mcm.backend.app.database.core.components.daos.DAO;
import com.mcm.backend.app.database.core.components.daos.querying.QueryBuilder;
import com.mcm.backend.app.database.core.factories.DAOFactory;
import com.mcm.backend.app.database.models.logging.ActionType;
import com.mcm.backend.app.database.models.roles.Role;
import com.mcm.backend.app.database.models.server.ServerInstance;
import com.mcm.backend.app.database.models.server.ServerInstanceProperty;
import com.mcm.backend.app.database.models.server.utils.ServerPropertiesUtil;
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
@RequestMapping("/api/server-instances/{serverInstanceId}")
public class ServerInstancePropertiesController {

    @GetMapping("/properties")
    @RequireServerInstanceRole(Role.EDITOR)
    public ResponseEntity<List<ServerInstanceProperty>> getServerInstanceProperties(@PathVariable UUID serverInstanceId) throws JsonErrorResponseException, NoSuchFieldException {
        try (DAO<ServerInstanceProperty, UUID> sipDAO = DAOFactory.createDAO(ServerInstanceProperty.class);
             DAO<ServerInstance, UUID> serverInstanceDAO = DAOFactory.createDAO(ServerInstance.class)) {

            if (!serverInstanceDAO.existsByPrimaryKey(serverInstanceId)) {
                throw new JsonErrorResponseException("Server instance not found", HttpStatus.NOT_FOUND);
            }

            return ResponseEntity.ok(
                    new QueryBuilder<>(sipDAO)
                            .where(ServerInstanceProperty.class.getDeclaredField("serverInstanceId"), serverInstanceId)
                            .and(ServerInstanceProperty.class.getDeclaredField("hidden"), false)
                            .orderBy(ServerInstanceProperty.class.getDeclaredField("type"))
                            .get()
            );
        }
    }

    @PostMapping("/property/{propertyId}")
    @RequireServerInstanceRole(Role.EDITOR)
    public ResponseEntity<?> setServerInstanceProperty(@PathVariable UUID serverInstanceId, @PathVariable UUID propertyId, @CurrentUser User currentUser, RequestBodyUtil requestBodyUtil) throws JsonErrorResponseException {

        String value = requestBodyUtil.getField("value", String.class);

        try (DAO<ServerInstanceProperty, UUID> sipDAO = DAOFactory.createDAO(ServerInstanceProperty.class);
             DAO<ServerInstance, UUID> serverInstanceDAO = DAOFactory.createDAO(ServerInstance.class)) {

            ServerInstance serverInstance = serverInstanceDAO.get(serverInstanceId);

            if (serverInstance == null) {
                throw new JsonErrorResponseException("Server instance not found", HttpStatus.NOT_FOUND);
            }

            ServerInstanceProperty serverInstanceProperty = sipDAO.get(propertyId);

            // Check if the property exists
            if (serverInstanceProperty == null) {
                throw new JsonErrorResponseException("Server instance property not found", HttpStatus.NOT_FOUND);
            }

            // Check if the property is linked to the instance
            if (!serverInstanceProperty.getServerInstanceId().equals(serverInstanceId)) {
                throw new JsonErrorResponseException("Server instance property not found", HttpStatus.NOT_FOUND);
            }

            // Save old value for logging
            String oldValue = serverInstanceProperty.getValue();

            // Update value
            serverInstanceProperty.setValue(value);

            // Update database
            sipDAO.update(serverInstanceProperty);

            // Log action
            LoggingUtil.log(ActionType.EDIT_PROPERTY, currentUser, serverInstance, Map.of(
                    "property_name", serverInstanceProperty.getKey(),
                    "old_value", oldValue,
                    "new_value", value
            ));

            return ResponseEntity.ok().build();

        }
    }

    @PostMapping("/write-properties")
    @RequireServerInstanceRole(Role.EDITOR)
    public ResponseEntity<?> writeServerInstanceProperties(@PathVariable UUID serverInstanceId) throws JsonErrorResponseException, NoSuchFieldException {
        try (DAO<ServerInstanceProperty, UUID> sipDAO = DAOFactory.createDAO(ServerInstanceProperty.class);
             DAO<ServerInstance, UUID> serverInstanceDAO = DAOFactory.createDAO(ServerInstance.class)) {

            ServerInstance serverInstance = serverInstanceDAO.get(serverInstanceId);

            if (serverInstance == null) {
                throw new JsonErrorResponseException("Server instance not found", HttpStatus.NOT_FOUND);
            }

            if (serverInstance.isRunning()) {
                throw new JsonErrorResponseException("Server Instance running", HttpStatus.CONFLICT);
            }

            // Get all relevant properties
            List<ServerInstanceProperty> properties = new QueryBuilder<>(sipDAO)
                    .where(ServerInstanceProperty.class.getDeclaredField("serverInstanceId"), serverInstanceId)
                    .get();

            // Write the properties
            ServerPropertiesUtil.write(serverInstance, properties);

            return ResponseEntity.ok().build();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
