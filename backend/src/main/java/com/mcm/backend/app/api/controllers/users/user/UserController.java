package com.mcm.backend.app.api.controllers.users.user;

import com.mcm.backend.app.api.utils.LoggingUtil;
import com.mcm.backend.app.api.utils.PasswordHashUtil;
import com.mcm.backend.app.api.utils.annotations.CurrentUser;
import com.mcm.backend.app.api.utils.annotations.RequireRole;
import com.mcm.backend.app.database.core.components.daos.DAO;
import com.mcm.backend.app.database.core.factories.DAOFactory;
import com.mcm.backend.app.database.models.logging.ActionType;
import com.mcm.backend.app.database.models.users.Admin;
import com.mcm.backend.app.database.models.users.User;
import com.mcm.backend.app.api.utils.annotations.ValidatedBody;
import com.mcm.backend.exceptions.JsonErrorResponseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static com.mcm.backend.app.api.controllers.users.user.Utils.usernameInUse;

@RestController
@RequestMapping("/api/users")
public class UserController {

    /**
     * List all users.
     */
    @GetMapping
    @RequireRole(Admin.class)
    public ResponseEntity<List<User>> getAllUsers() {

        // TODO Add bool if admin

        // Get all users from the DB
        try (DAO<User, UUID> userDAO = DAOFactory.createDAO(User.class)) {
            return ResponseEntity.ok(new ArrayList<>(userDAO.getAll()));
        }
    }

    /**
     * Get a single user by UUID.
     *
     * @param id user ID
     * @return The user, if found
     */
    @GetMapping("/{id}")
    @RequireRole(Admin.class)
    public ResponseEntity<?> getUser(@PathVariable UUID id) {
        User user;

        // Get the user from the DB
        try (DAO<User, UUID> userDAO = DAOFactory.createDAO(User.class)) {
            user = userDAO.get(id);
        }

        // Handle User not found
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        // TODO Add bool if admin

        // Handle User found
        return ResponseEntity.ok(user);
    }

    /**
     * Check if a username is in use
     * @param username The username that will be checked
     * @return {@code {'in_use', bool}}
     */
    @GetMapping("/username-in-use/{username}")
    @RequireRole(User.class)
    public ResponseEntity<?> getUsernameInUse(@PathVariable String username) {
        boolean inUse;
        try (DAO<User, UUID> userDAO = DAOFactory.createDAO(User.class)) {
            inUse = usernameInUse(userDAO, username);
        }
        return ResponseEntity.ok(Map.of("in_use", inUse));
    }

    /**
     * Create a new user using a validated request body.
     *
     * @param user The user to create
     * @return Created user
     *
     */
    @PostMapping
    @RequireRole(Admin.class)
    public ResponseEntity<User> createUser(@CurrentUser User currentUser, @ValidatedBody(User.class) User user, @CurrentUser Admin admin) throws JsonErrorResponseException {
        try (DAO<User, UUID> userDAO = DAOFactory.createDAO(User.class)) {

            // Check if the username is unique
            if (usernameInUse(userDAO, user)) {
                throw new JsonErrorResponseException("username " + user.getUsername() + " already in use");
            }

            // Add the user
            userDAO.add(user);

            // Log the action
            LoggingUtil.log(ActionType.USER_CREATE, currentUser, user);

            // Return with success
            return ResponseEntity.ok(user);

        }
    }

    /**
     * Update an existing user.
     *
     * @param id   the ID of the user to update
     * @param user the updated user data
     * @return updated user or 404 if not found
     */
    @PutMapping("/{id}")
    @RequireRole(Admin.class)
    public ResponseEntity<?> updateUser(@CurrentUser User currentUser, @PathVariable UUID id, @ValidatedBody(User.class) User user) throws JsonErrorResponseException {
        try (DAO<User, UUID> userDAO = DAOFactory.createDAO(User.class)) {

            // Ensure the ID from the path matches the one in the request
            if (!user.getId().equals(id)) {
                throw new JsonErrorResponseException("ID in path and body must match");
            }

            User oldUser = userDAO.get(id);

            if (oldUser == null) {
                throw new JsonErrorResponseException("User not found", HttpStatus.NOT_FOUND);
            }

            String changedField = "?";
            String oldValue = "-";
            String newValue = "-";

            // If username changed
            if (!oldUser.getUsername().equals(user.getUsername())) {
                // Check if the username is unique
                if (usernameInUse(userDAO, user)) {
                    throw new JsonErrorResponseException("Username already in use");
                }

                // Set vars for logging
                changedField = "username";
                oldValue = oldUser.getUsername();

            }

            // If password changed
            if (!oldUser.getPasswordHash().equals(user.getPasswordHash())) {

                // The password is not hashed yet. however the getPasswordHash method lets it appear that way
                String password = user.getPasswordHash();

                // Let the user setPassword hash the password and store it
                user.setPassword(password);

                // Set vars for logging
                changedField = "password";
                // Keep values "-" (redacted)
            }

            userDAO.update(user);

            LoggingUtil.log(ActionType.USER_UPDATE, currentUser, user, Map.of(
                    "updated_field", changedField,
                    "old_value", oldValue,
                    "new_value", newValue
            ));

            return ResponseEntity.ok(user.getId());
        }
    }

    /**
     * Promote user to admin.
     */
    @PostMapping("/{id}")
    @RequireRole(Admin.class)
    public ResponseEntity<?> promote(@CurrentUser User currentUser, @PathVariable UUID id) throws JsonErrorResponseException {
        // Get all users from the DB
        try (DAO<User, UUID> userDAO = DAOFactory.createDAO(User.class)) {
            User user = userDAO.get(id);
            if (user == null) {
                throw new JsonErrorResponseException("User with id " + id.toString() + " not found", HttpStatus.NOT_FOUND);
            }

            try (DAO<Admin, UUID> adminDAO = DAOFactory.createDAO(Admin.class)) {

                Admin admin = new Admin(user);

                // Check if user already admin
                if (adminDAO.exists(admin)) {
                    throw new JsonErrorResponseException("Admin with id " + id.toString() + " already exists", HttpStatus.CONFLICT);
                }

                // Add user to admin table (promote to admin)
                adminDAO.add(admin);

                // Log event
                LoggingUtil.log(ActionType.USER_PROMOTE, currentUser, user);

                // Give response
                return ResponseEntity.ok("Promoted user with id " + id.toString() + " to admin");
            }
        }
    }

    /**
     * Delete a user by ID.
     *
     * @param id the user's UUID
     */
    @DeleteMapping("/{id}")
    @RequireRole(Admin.class)
    public ResponseEntity<?> deleteUser(@CurrentUser User currentUser, @PathVariable UUID id) {
        try (DAO<User, UUID> userDAO = DAOFactory.createDAO(User.class)) {
            if (userDAO.existsByPrimaryKey(id)) {

                User user = userDAO.get(id);

                userDAO.delete(id);

                LoggingUtil.log(ActionType.USER_DELETE, currentUser, Map.of("deleted_user_username", user.getUsername()));

                return ResponseEntity.ok(Map.of("message", "Deleted", "id", id.toString()));
            } else {
                return ResponseEntity.notFound().build();
            }
        }
    }
}
