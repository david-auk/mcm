package com.mcm.backend.app.database.models.users;

import com.mcm.backend.app.database.core.annotations.table.TableConstructor;
import com.mcm.backend.app.database.core.components.tables.AutoTableEntity;
import com.mcm.backend.app.database.core.annotations.table.Id;
import com.mcm.backend.app.database.core.annotations.table.TableField;
import com.mcm.backend.app.database.core.annotations.table.TableName;

import java.util.Objects;
import java.util.UUID;

@TableName("users")
public class User implements AutoTableEntity {

    @Id(UUID.class)
    private final UUID id;

    @TableField(type = String.class)
    private String username;

    @TableField(name = "password_hash", type = String.class)
    private String passwordHash;

    @TableConstructor
    public User(UUID id, String username, String passwordHash) {
        this.id = Objects.requireNonNullElseGet(id, UUID::randomUUID);
        this.username = username;
        this.passwordHash = passwordHash;
    }

    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    @Override
    public String toString() {
        return "{id: " + id + ", username: " + username + ", passwordHash: " + passwordHash + "}";
    }
}
