package com.mcm.backend.app.api.controllers.serverinstances.roles;

import com.mcm.backend.app.api.utils.annotations.CurrentUser;
import com.mcm.backend.app.api.utils.annotations.RequireRole;
import com.mcm.backend.app.database.core.components.daos.DAO;
import com.mcm.backend.app.database.core.factories.DAOFactory;
import com.mcm.backend.app.database.models.roles.Role;
import com.mcm.backend.app.database.models.roles.RoleEntity;
import com.mcm.backend.app.database.models.roles.RoleInheritance;
import com.mcm.backend.app.database.models.server.ServerInstance;
import com.mcm.backend.app.database.models.users.User;
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
@RequestMapping("/api/server-instances/me")
public class RoleMeController {

    @GetMapping
    @RequireRole(User.class)
    public ResponseEntity<List<ServerInstance>> getViewableServerInstances(@CurrentUser User user) throws NoSuchFieldException {
        List<ServerInstance> serverInstances = RoleUtil.getServerInstances(Role.VIEWER, user.getId());

        return ResponseEntity.ok(serverInstances);
    }

    @GetMapping("/{id}/roles")
    @RequireRole(User.class)
    public ResponseEntity<List<RoleEntity>> getRoles(@PathVariable UUID id, @CurrentUser User user) throws JsonErrorResponseException, NoSuchFieldException {

        // Check if server exists
        try (DAO<ServerInstance, UUID> serverInstanceDAO = DAOFactory.createDAO(ServerInstance.class)) {
            if (!serverInstanceDAO.existsByPrimaryKey(id)) {
                throw new JsonErrorResponseException("Server not found", HttpStatus.NOT_FOUND);
            }
        }

        // Get the current users role assignment for this serverInstance
        UserRoleAssignment userRoleAssignment = RoleUtil.fetchUserRoleAssignment(user.getId(), id);

        // If the user has no roles assigned; they are not permitted to view
        if (userRoleAssignment == null) {
            throw new JsonErrorResponseException("Server not found", HttpStatus.NOT_FOUND);
        }

        // Build the sting to role
        RoleEntity assignedRole;
        List<RoleEntity> roles;
        try (
                DAO<RoleEntity, String> roleDAO = DAOFactory.createDAO(RoleEntity.class);
                DAO<RoleInheritance, String> roleInheritanceDAO = DAOFactory.createDAO(RoleInheritance.class)
        ){
            assignedRole = roleDAO.get(userRoleAssignment.getRole());

            // Get all the (Inherited) roles
            roles = RoleUtil.fetchAllInheritedRoles(assignedRole, roleDAO, roleInheritanceDAO);
        }

        // Return a list of roles
        return ResponseEntity.ok(roles);
    }
}
