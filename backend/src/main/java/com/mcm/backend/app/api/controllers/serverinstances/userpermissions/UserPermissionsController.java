package com.mcm.backend.app.api.controllers.serverinstances.userpermissions;

import com.mcm.backend.app.api.utils.annotations.CurrentUser;
import com.mcm.backend.app.api.utils.annotations.RequireRole;
import com.mcm.backend.app.api.utils.annotations.ValidatedBody;
import com.mcm.backend.app.api.utils.requestbody.RequestBodyUtil;
import com.mcm.backend.app.database.models.server.ServerInstance;
import com.mcm.backend.app.database.models.users.Admin;
import com.mcm.backend.app.database.models.users.User;
import com.mcm.backend.app.database.models.users.UserRoleAssignment;
import com.mcm.backend.exceptions.JsonErrorResponseException;
import io.github.david.auk.fluid.jdbc.components.Database;
import io.github.david.auk.fluid.jdbc.components.daos.DAO;
import io.github.david.auk.fluid.jdbc.components.daos.querying.QueryBuilder;
import io.github.david.auk.fluid.jdbc.factories.DAOFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/server-instance/{id}/permissions")
public class UserPermissionsController {

    @GetMapping
    @RequireRole(Admin.class)
    public ResponseEntity<List<UserRoleAssignment>> getServerUserPermissions(@PathVariable UUID id) throws JsonErrorResponseException {
        try (Connection connection = Database.getConnection();
             DAO<ServerInstance, UUID> serverInstanceDAO = DAOFactory.createDAO(connection, ServerInstance.class);
             DAO<UserRoleAssignment, UUID> userRoleAssignmentDAO = DAOFactory.createDAO(connection, UserRoleAssignment.class);
        ) {
            ServerInstance serverInstance = serverInstanceDAO.get(id);

            if (serverInstance == null) {
                throw new JsonErrorResponseException("Server instance not found", HttpStatus.NOT_FOUND);
            }

            List<UserRoleAssignment> userPermissions = new QueryBuilder<>(userRoleAssignmentDAO)
                    .where(UserRoleAssignment.class.getDeclaredField("serverInstance"), serverInstance.getId())
                    .get();

            return ResponseEntity.ok(userPermissions);
        } catch (SQLException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping
    @RequireRole(Admin.class)
    public ResponseEntity<UserRoleAssignment> createServerUserPermission(RequestBodyUtil requestBodyUtil, @PathVariable UUID id) throws JsonErrorResponseException {
        String role = requestBodyUtil.getField("role", String.class);
        UUID userId = requestBodyUtil.getField("user_id", UUID.class);

        try (Connection connection = Database.getConnection();
             DAO<ServerInstance, UUID> serverInstanceDAO = DAOFactory.createDAO(connection, ServerInstance.class);
             DAO<UserRoleAssignment, UUID> userRoleAssignmentDAO = DAOFactory.createDAO(connection, UserRoleAssignment.class);
             DAO<User, UUID> userDAO = DAOFactory.createDAO(connection, User.class)) {

            // 1) Validate server exists
            ServerInstance serverInstance = serverInstanceDAO.get(id);
            if (serverInstance == null) {
                throw new JsonErrorResponseException("Server instance not found", HttpStatus.NOT_FOUND);
            }

            // 2) Validate target user exists
            User targetUser = userDAO.get(userId);
            if (targetUser == null) {
                throw new JsonErrorResponseException("User not found", HttpStatus.NOT_FOUND);
            }

            // 3) Prevent duplicate assignment (same server + user)
            List<UserRoleAssignment> existing = new QueryBuilder<>(userRoleAssignmentDAO)
                    .where(UserRoleAssignment.class.getDeclaredField("serverInstance"), serverInstance.getId())
                    .where(UserRoleAssignment.class.getDeclaredField("user"), targetUser.getId())
                    .get();
            if (!existing.isEmpty()) {
                throw new JsonErrorResponseException("Permission already exists for this user on this server", HttpStatus.CONFLICT);
            }

            // 4) Create and insert the new assignment
            // NOTE: Adjust constructor/field mapping if your model differs (e.g., enum vs string for role)
            UserRoleAssignment assignment = new UserRoleAssignment(null, targetUser, serverInstance, role);
            userRoleAssignmentDAO.add(assignment);

            return ResponseEntity.status(HttpStatus.CREATED).body(assignment);
        } catch (SQLException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @PutMapping("/{permissionId}")
    @RequireRole(Admin.class)
    public ResponseEntity<UserRoleAssignment> updateServerUserPermission(
            @PathVariable UUID id,
            @PathVariable UUID permissionId,
            RequestBodyUtil requestBodyUtil
    ) throws JsonErrorResponseException {
        String newRole = requestBodyUtil.getField("role", String.class);

        try (Connection connection = Database.getConnection();
             DAO<ServerInstance, UUID> serverInstanceDAO = DAOFactory.createDAO(connection, ServerInstance.class);
             DAO<UserRoleAssignment, UUID> userRoleAssignmentDAO = DAOFactory.createDAO(connection, UserRoleAssignment.class)) {

            // 1) Validate server instance exists
            ServerInstance serverInstance = serverInstanceDAO.get(id);
            if (serverInstance == null) {
                throw new JsonErrorResponseException("Server instance not found", HttpStatus.NOT_FOUND);
            }

            // 2) Fetch target permission
            UserRoleAssignment assignment = userRoleAssignmentDAO.get(permissionId);
            if (assignment == null) {
                throw new JsonErrorResponseException("Permission not found", HttpStatus.NOT_FOUND);
            }

            // 3) Verify that the permission belongs to this server
            if (!assignment.getServerInstance().getId().equals(serverInstance.getId())) {
                throw new JsonErrorResponseException("Permission does not belong to this server instance", HttpStatus.FORBIDDEN);
            }

            // 4) Update the role field
            assignment.setRole(newRole);
            userRoleAssignmentDAO.update(assignment);

            return ResponseEntity.ok(assignment);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}