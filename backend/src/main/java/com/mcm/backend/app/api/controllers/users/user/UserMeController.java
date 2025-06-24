package com.mcm.backend.app.api.controllers.users.user;

import com.mcm.backend.app.api.utils.LoggingUtil;
import com.mcm.backend.app.api.utils.RequestBodyUtil;
import com.mcm.backend.app.api.utils.annotations.CurrentUser;
import com.mcm.backend.app.api.utils.annotations.RequireRole;
import com.mcm.backend.app.database.core.components.daos.DAO;
import com.mcm.backend.app.database.core.factories.DAOFactory;
import com.mcm.backend.app.database.models.logging.ActionType;
import com.mcm.backend.app.database.models.users.User;
import com.mcm.backend.exceptions.JsonErrorResponseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

import static com.mcm.backend.app.api.controllers.users.user.Utils.usernameInUse;

@RestController
@RequestMapping("/api/user/me")
public class UserMeController {

    @RequireRole(User.class)
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@CurrentUser User user) {
        return ResponseEntity.ok(user);
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
}