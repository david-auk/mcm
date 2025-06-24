package com.mcm.backend.app.database.models.logging;

import com.mcm.backend.app.database.core.annotations.table.PrimaryKey;
import com.mcm.backend.app.database.core.annotations.table.TableConstructor;
import com.mcm.backend.app.database.core.annotations.table.TableField;
import com.mcm.backend.app.database.core.annotations.table.TableName;
import com.mcm.backend.app.database.core.components.tables.TableEntity;

import java.sql.Timestamp;
import java.util.*;

@TableName("user_action_logs")
public record UserActionLog(
        @PrimaryKey(UUID.class)
        UUID id,

        @TableField(type = String.class, name = "action_type")
        String actionType,

        @TableField(type = UUID.class, name = "user_id")
        UUID userId,

        @TableField(type = UUID.class, name = "affected_user_id")
        UUID affectedUserId,

        @TableField(type = UUID.class, name = "instance_id")
        UUID instanceId,

        @TableField(type = Timestamp.class)
        Timestamp timestamp,

        @TableField(type = Map.class) // raw Map, handled specially in DAO
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
