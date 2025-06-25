package com.mcm.backend.app.api.controllers.users.user;

import com.mcm.backend.app.api.utils.LoggingUtil;
import com.mcm.backend.app.api.utils.PasswordHashUtil;
import com.mcm.backend.app.api.utils.RequestBodyUtil;
import com.mcm.backend.app.api.utils.annotations.CurrentUser;
import com.mcm.backend.app.api.utils.annotations.RequireRole;
import com.mcm.backend.app.database.core.components.daos.DAO;
import com.mcm.backend.app.database.core.components.daos.querying.QueryBuilder;
import com.mcm.backend.app.database.core.factories.DAOFactory;
import com.mcm.backend.app.database.models.logging.ActionType;
import com.mcm.backend.app.database.models.logging.ActionTypeEntity;
import com.mcm.backend.app.database.models.logging.UserActionLog;
import com.mcm.backend.app.database.models.server.ServerInstance;
import com.mcm.backend.app.database.models.users.User;
import com.mcm.backend.exceptions.JsonErrorResponseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.mcm.backend.app.api.controllers.users.user.Utils.usernameInUse;

@RestController
@RequestMapping("/api/user/me")
public class UserMeController {

    @RequireRole(User.class)
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@CurrentUser User user, @RequestBody Map<String, ?> body) throws JsonErrorResponseException {

        // Get the passwords
        RequestBodyUtil requestBodyUtil = new RequestBodyUtil(body);
        String currentPassword = requestBodyUtil.getField("current_password", String.class);
        String newPassword = requestBodyUtil.getField("new_password", String.class);

        // TODO optional lint here

        // Store the old passwordHash for check
        String oldPasswordHash = user.getPasswordHash();

        // Check if the sent password matches the current password
        if (!oldPasswordHash.equals(PasswordHashUtil.hashPassword(currentPassword))) {
            throw new JsonErrorResponseException("Current password incorrect", HttpStatus.BAD_REQUEST);
        }

        // Hash and store the password
        user.setPassword(newPassword);

        // If the password is unchanged
        if (user.getPasswordHash().equals(oldPasswordHash)) {
            throw new JsonErrorResponseException("Password unchanged", HttpStatus.BAD_REQUEST);
        }

        try (DAO<User, UUID> userDAO = DAOFactory.createDAO(User.class)) {
            // Store the new password
            userDAO.update(user);

            // Log the action
            LoggingUtil.log(ActionType.USER_CHANGED_PASSWORD, user);

            // Send OK signal
            return ResponseEntity.ok().build();
        }
    }

    @RequireRole(User.class)
    @PostMapping("/change-username")
    public ResponseEntity<?> changeUsername(@CurrentUser User user, @RequestBody Map<String, ?> body) throws JsonErrorResponseException {

        // Get the new username
        RequestBodyUtil requestBodyUtil = new RequestBodyUtil(body);
        String newUsername = requestBodyUtil.getField("new_username", String.class);

        try (DAO<User, UUID> userDAO = DAOFactory.createDAO(User.class)) {
            // Check if username is already in use
            if (usernameInUse(userDAO, newUsername)) {
                throw new JsonErrorResponseException("Username already in use", HttpStatus.CONFLICT);
            }

            // Save old username for logging
            String oldUsername = user.getUsername();

            // Update the user
            user.setUsername(newUsername);

            // Update the database
            userDAO.update(user);

            // Log the action
            LoggingUtil.log(ActionType.USER_CHANGED_USERNAME, user, Map.of("old_username", oldUsername, "new_username", newUsername));

            return ResponseEntity.ok().build();
        }
    }

    @RequireRole(User.class)
    @GetMapping("/notifications")
    public ResponseEntity<?> getNotifications(@CurrentUser User user) throws NoSuchFieldException {

        // Define an empty list to be populated by notifications
        ArrayList<Map<String, Object>> notifications = new ArrayList<>();

        // Open a new UserActionLog DAO
        try (DAO<UserActionLog, UUID> userActionLogDAO = DAOFactory.createDAO(UserActionLog.class)) {

            // Open a new User DAO
            try (DAO<User, UUID> userDAO = DAOFactory.createDAO(User.class)) {

                // Open a new Server instance DAO
                try (DAO<ServerInstance, UUID> serverInstanceDAO = DAOFactory.createDAO(ServerInstance.class)) {

                    // Open a new actionType DAO
                    try (DAO<ActionTypeEntity, String> actionTypeDAO = DAOFactory.createDAO(ActionTypeEntity.class)) {

                        // Build a list of UserActionLogs
                        List<UserActionLog> userActionLogs = new QueryBuilder<>(userActionLogDAO)
                                .where(UserActionLog.class.getDeclaredField("userId"), user.getId())
                                .orderBy(UserActionLog.class.getDeclaredField("timestamp"))
                                .desc()
                                .get();

                        // Format each result using the LoggingUtil
                        for (UserActionLog userActionLog : userActionLogs) {
                            notifications.add(Map.of(
                                "message_template", actionTypeDAO.get(userActionLog.actionType()),
                                "vars", LoggingUtil.getMetadata(userActionLog, userDAO, serverInstanceDAO),
                                "timestamp", userActionLog.timestamp()
                            ));
                        }

                        // Return the formatted list of notifications
                        return ResponseEntity.ok(notifications);
                    }
                }
            }
        }
    }

    @RequireRole(User.class)
    @DeleteMapping
    public ResponseEntity<?> deleteUser(@CurrentUser User user) {
        try (DAO<User, UUID> userDAO = DAOFactory.createDAO(User.class)) {
            userDAO.delete(user.getId());

            // TODO Revoke current Token

            // TODO fix self deletion log (right now FK constraint fails because the deleted user cant be found)
            //LoggingUtil.log(ActionType.USER_DELETE, user, Map.of("deleted_user_username", user.getUsername()));

            return ResponseEntity.ok().build();
        }
    }
}