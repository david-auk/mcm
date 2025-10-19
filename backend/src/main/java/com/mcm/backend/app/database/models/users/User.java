package com.mcm.backend.app.database.models.users;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mcm.backend.app.api.utils.PasswordHashUtil;
import com.mcm.backend.app.database.models.roles.RoleEntity;
import com.mcm.backend.app.database.models.server.ServerInstance;
import com.mcm.backend.app.middlewares.data.roles.RoleDAO;
import com.mcm.backend.app.middlewares.data.serverinstances.ServerInstanceUtil;
import com.mcm.backend.exceptions.JsonErrorResponseException;
import io.github.david.auk.fluid.jdbc.annotations.table.*;
import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;

import java.sql.Connection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@TableName("users")
public class User implements TableEntity {

    @TableColumn
    @PrimaryKey
    private final UUID id;

    @TableColumn
    @UniqueColumn
    private String username;

    @TableColumn(name = "password_hash")
    @Nullable
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

    @JsonIgnore // Make the password not serialized
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

    public void setPassword(String password) {
        setPasswordHash(PasswordHashUtil.hashPassword(password));
    }

    // DB METHODS

    public List<ServerInstance> getServerInstances(Connection connection) {
        return ServerInstanceUtil.getServerInstances(connection, this);
    }

    public List<RoleEntity> getRoles(Connection connection, ServerInstance serverInstance) throws JsonErrorResponseException, NoSuchFieldException {
        return RoleDAO.getRolesForInstance(connection, serverInstance, this);
    }
}
