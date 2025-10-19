package com.mcm.backend.app.api.controllers.serverinstances.roles;

import com.mcm.backend.app.api.utils.annotations.CurrentUser;
import com.mcm.backend.app.api.utils.annotations.RequireRole;
import io.github.david.auk.fluid.jdbc.components.Database;
import io.github.david.auk.fluid.jdbc.components.daos.DAO;
import io.github.david.auk.fluid.jdbc.factories.DAOFactory;
import com.mcm.backend.app.database.models.roles.RoleEntity;
import com.mcm.backend.app.database.models.server.ServerInstance;
import com.mcm.backend.app.database.models.users.User;
import com.mcm.backend.app.middlewares.data.serverinstances.ServerInstanceUtil;
import com.mcm.backend.exceptions.JsonErrorResponseException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/server-instances/me")
public class RoleMeController {

    @GetMapping
    @RequireRole(User.class)
    public ResponseEntity<List<ServerInstance>> getViewableServerInstances(@CurrentUser User user) throws SQLException {
        try (Connection connection = Database.getConnection()){
            List<ServerInstance> serverInstances = user.getServerInstances(connection);
            return ResponseEntity.ok(serverInstances);
        }
    }

    @GetMapping("/{id}/roles")
    @RequireRole(User.class)
    public ResponseEntity<List<RoleEntity>> getRoles(@PathVariable UUID id, @CurrentUser User user) throws JsonErrorResponseException, NoSuchFieldException, SQLException {

        try (
            Connection connection = Database.getConnection();
            DAO<ServerInstance, UUID> serverInstanceDao = DAOFactory.createDAO(connection, ServerInstance.class)
        ){
            ServerInstance serverInstance = serverInstanceDao.get(id);

            if (serverInstance == null) {
                throw new JsonErrorResponseException("Server instance not found");
            }

            return ResponseEntity.ok(user.getRoles(connection, serverInstance));
        }
    }
}
