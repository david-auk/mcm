package com.mcm.backend.app.database.models.logging;

import com.mcm.backend.app.database.core.annotations.table.PrimaryKey;
import com.mcm.backend.app.database.core.annotations.table.TableConstructor;
import com.mcm.backend.app.database.core.annotations.table.TableColumn;
import com.mcm.backend.app.database.core.annotations.table.TableName;
import com.mcm.backend.app.database.core.components.tables.TableEntity;

import java.sql.Timestamp;
import java.util.*;

@TableName("user_action_logs")
public record UserActionLog(

        @TableColumn
        @PrimaryKey
        UUID id,

        @TableColumn(name = "action_type")
        String actionType,

        @TableColumn(name = "user_id")
        UUID userId,

        @TableColumn(name = "affected_user_id")
        UUID affectedUserId,

        @TableColumn(name = "instance_id")
        UUID instanceId,

        @TableColumn
        Timestamp timestamp,

        @TableColumn
        Map<String, Object> metadata
) implements TableEntity {

    @TableConstructor

    public UserActionLog(UUID id, String actionType, UUID userId, UUID affectedUserId, UUID instanceId, Timestamp timestamp, Map<String, Object> metadata) {
        this.id = Objects.requireNonNullElseGet(id, UUID::randomUUID);

        if (actionType == null) {
            throw new IllegalArgumentException("actionType cannot be null");
        }
        if (Arrays.stream(ActionType.values()).noneMatch(a -> a.name().equalsIgnoreCase(actionType))) {
            throw new IllegalArgumentException("actionType " + actionType + " is not supported");
        }
        this.actionType = actionType;
        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null");
        }
        this.userId = userId;
        this.affectedUserId = affectedUserId;
        this.instanceId = instanceId;
        this.timestamp = Objects.requireNonNullElseGet(timestamp, () -> new Timestamp(System.currentTimeMillis()));
        this.metadata = metadata;
    }
}
