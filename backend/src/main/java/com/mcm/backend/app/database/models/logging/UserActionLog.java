package com.mcm.backend.app.database.models.logging;

import com.mcm.backend.app.database.core.annotations.table.*;
import com.mcm.backend.app.database.core.components.tables.TableEntity;
import com.mcm.backend.app.database.models.server.ServerInstance;
import com.mcm.backend.app.database.models.users.User;

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
        @ForeignKey
        User user,

        @TableColumn(name = "affected_user_id")
        @ForeignKey
        @Nullable
        User affectedUser,

        @TableColumn(name = "instance_id")
        @ForeignKey
        @Nullable
        ServerInstance serverInstance,

        @TableColumn
        @Nullable
        Timestamp timestamp,

        @TableColumn
        Map<String, Object> metadata
) implements TableEntity {

    @TableConstructor
    public UserActionLog(
            UUID id,
            String actionType,
            User user,
            User affectedUser,
            ServerInstance serverInstance,
            Timestamp timestamp,
            Map<String, Object> metadata) {
        this.id = Objects.requireNonNullElseGet(id, UUID::randomUUID);

        if (actionType == null) {
            throw new IllegalArgumentException("actionType cannot be null");
        }
        if (Arrays.stream(ActionType.values()).noneMatch(a -> a.name().equalsIgnoreCase(actionType))) {
            throw new IllegalArgumentException("ActionType " + actionType + " is not supported");
        }
        this.actionType = actionType;
        if (user == null) {
            throw new IllegalArgumentException("user cannot be null");
        }
        this.user = user;
        this.affectedUser = affectedUser;
        this.serverInstance = serverInstance;
        this.timestamp = Objects.requireNonNullElseGet(timestamp, () -> new Timestamp(System.currentTimeMillis()));
        this.metadata = metadata;
    }
}
