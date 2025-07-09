package com.mcm.backend.app.database.models.server.backups;

import com.mcm.backend.app.database.core.annotations.table.*;
import com.mcm.backend.app.database.core.components.tables.TableEntity;
import com.mcm.backend.app.database.models.server.ServerInstance;
import com.mcm.backend.app.database.models.users.User;

import java.sql.Timestamp;
import java.util.Objects;
import java.util.UUID;

@TableName("backups")
public record Backup(@PrimaryKey @TableColumn UUID id,
                     @TableColumn(name = "server_instance_id") @ForeignKey ServerInstance serverInstance,
                     @TableColumn(name = "created_by") @ForeignKey User createdBy,
                     @Nullable @TableColumn Timestamp timestamp) implements TableEntity {

    @TableConstructor
    public Backup(UUID id, ServerInstance serverInstance, User createdBy, Timestamp timestamp) {
        this.id = Objects.requireNonNullElseGet(id, UUID::randomUUID);
        if (serverInstance == null) {
            throw new IllegalArgumentException("serverInstance cannot be null");
        }
        this.serverInstance = serverInstance;
        if (createdBy == null) {
            throw new IllegalArgumentException("Created by cannot be null");
        }
        this.createdBy = createdBy;
        this.timestamp = Objects.requireNonNullElseGet(timestamp, () -> new Timestamp(System.currentTimeMillis()));
    }
}
