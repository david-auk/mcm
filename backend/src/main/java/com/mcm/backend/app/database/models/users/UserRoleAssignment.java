package com.mcm.backend.app.database.models.users;

import io.github.david.auk.fluid.jdbc.annotations.table.*;
import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;
import com.mcm.backend.app.database.models.roles.Role;
import com.mcm.backend.app.database.models.server.ServerInstance;

import java.util.Objects;
import java.util.UUID;

@TableName("user_role_assignments")
public class UserRoleAssignment implements TableEntity {

    @TableColumn
    @PrimaryKey
    private final UUID id;

    @TableColumn(name = "user_id")
    @ForeignKey
    private final User user;

    @TableColumn(name = "instance_id")
    @ForeignKey
    private final ServerInstance serverInstance;

    @TableColumn
    private String role;

    @TableConstructor
    public UserRoleAssignment(UUID id, User user, ServerInstance serverInstance, String role) {
        this.id = Objects.requireNonNullElseGet(id, UUID::randomUUID);

        // User validation
        if (user == null) {
            throw new IllegalArgumentException("user cannot be null");
        }
        this.user = user;

        // Instance validation
        if (serverInstance == null) {
            throw new IllegalArgumentException("serverInstance cannot be null");
        }
        this.serverInstance = serverInstance;

        // Role validation
        setRole(role);
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public ServerInstance getServerInstance() {
        return serverInstance;
    }

    public String getRole() {
        return role;
    }

    // Role validation
    public void setRole(String role) {
        if (Role.isValidRole(role)) {
            this.role = role;
        } else {
            throw new IllegalArgumentException("Invalid role: " + role);
        }
    }
}
