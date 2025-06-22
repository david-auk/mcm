package com.mcm.backend.app.api.utils;

import com.mcm.backend.app.database.core.components.daos.DAO;
import com.mcm.backend.app.database.core.factories.DAOFactory;
import com.mcm.backend.app.database.models.logging.ActionType;
import com.mcm.backend.app.database.models.logging.UserActionLog;
import com.mcm.backend.app.database.models.server.ServerInstance;
import com.mcm.backend.app.database.models.users.User;

import java.util.Map;
import java.util.UUID;

public class LoggingUtil {

    public static void log(ActionType actionType, User user) {
        log(actionType, user, null, null, null);
    }

    public static void log(ActionType actionType, User user, User affectedUser) {
        log(actionType, user, affectedUser, null, null);
    }

    public static void log(ActionType actionType, User user, ServerInstance serverInstance) {
        log(actionType, user, null, serverInstance, null);
    }

    public static void log(ActionType actionType, User user, Map<String, Object> metadata) {
        log(actionType, user, null, null, metadata);
    }

    public static void log(ActionType actionType, User user, User affectedUser, ServerInstance serverInstance) {
        log(actionType, user, affectedUser, serverInstance, null);
    }

    public static void log(ActionType actionType, User user, User affectedUser, Map<String, Object> metadata) {
        log(actionType, user, affectedUser, null, metadata);
    }

    public static void log(ActionType actionType, User user, ServerInstance serverInstance, Map<String, Object> metadata) {
        log(actionType, user, null, serverInstance, metadata);
    }

    // Actual impl
    public static void log(ActionType actionType, User user, User affectedUser, ServerInstance serverInstance, Map<String, Object> metadata) {

        // Build the log instance
        UserActionLog userActionLog = new UserActionLog(
                null,
                actionType.toString(),
                user.getId(),
                affectedUser.getId(),
                serverInstance.getId(),
                null,
                metadata
        );

        // Log the action
        try (DAO<UserActionLog, UUID> userActionLogDAO = DAOFactory.createDAO(UserActionLog.class)) {
            userActionLogDAO.add(userActionLog);
        }
    }
}

