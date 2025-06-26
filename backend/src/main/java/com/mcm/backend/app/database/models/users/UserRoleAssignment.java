package com.mcm.backend.app.database.models.users;

import com.mcm.backend.app.database.core.annotations.table.PrimaryKey;
import com.mcm.backend.app.database.core.annotations.table.TableConstructor;
import com.mcm.backend.app.database.core.annotations.table.TableField;
import com.mcm.backend.app.database.core.annotations.table.TableName;
import com.mcm.backend.app.database.core.components.tables.TableEntity;
import com.mcm.backend.app.database.models.roles.Role;

import java.util.Objects;
import java.util.UUID;

@TableName("user_role_assignments")
public class UserRoleAssignment implements TableEntity {

    @PrimaryKey(UUID.class)
    private final UUID id;

    @TableField(name = "user_id", type = UUID.class)
    private final UUID userId;

    @TableField(name = "instance_id", type = UUID.class)
    private final UUID instanceId;

    @TableField(type = String.class)
    private String role;

    @TableConstructor
    public UserRoleAssignment(UUID id, UUID userId, UUID instanceId, String role) {
        this.id = Objects.requireNonNullElseGet(id, UUID::randomUUID);

        // User validation
        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null");
        }
        this.userId = userId;

        // Instance validation
        if (instanceId == null) {
            throw new IllegalArgumentException("instanceId cannot be null");
        }
        this.instanceId = instanceId;

        // Role validation
        setRole(role);
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getInstanceId() {
        return instanceId;
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
