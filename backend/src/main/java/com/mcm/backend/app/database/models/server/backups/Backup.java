package com.mcm.backend.app.database.models.server.backups;

import com.mcm.backend.app.database.core.annotations.table.*;
import com.mcm.backend.app.database.core.components.tables.TableEntity;

import java.sql.Timestamp;
import java.util.Objects;
import java.util.UUID;

@TableName("backups")
public record Backup(@PrimaryKey(UUID.class) UUID id,
                     @TableField(type = UUID.class, name = "server_instance_id") UUID serverInstanceId,
                     @TableField(type = UUID.class, name = "created_by") UUID createdBy,
                     @Nullable @TableField(type = Timestamp.class) Timestamp timestamp) implements TableEntity {

    @TableConstructor
    public Backup(UUID id, UUID serverInstanceId, UUID createdBy, Timestamp timestamp) {
        this.id = Objects.requireNonNullElseGet(id, UUID::randomUUID);
        if (serverInstanceId == null) {
            throw new IllegalArgumentException("Server instance id cannot be null");
        }
        this.serverInstanceId = serverInstanceId;
        if (createdBy == null) {
            throw new IllegalArgumentException("Created by cannot be null");
        }
        this.createdBy = createdBy;
        this.timestamp = Objects.requireNonNullElseGet(timestamp, () -> new Timestamp(System.currentTimeMillis()));
    }
}
