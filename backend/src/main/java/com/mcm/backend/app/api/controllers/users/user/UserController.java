package com.mcm.backend.app.api.controllers.users.user;

import com.mcm.backend.app.api.utils.annotations.CurrentUser;
import com.mcm.backend.app.api.utils.annotations.RequireRole;
import com.mcm.backend.app.database.core.components.daos.DAO;
import com.mcm.backend.app.database.core.factories.DAOFactory;
import com.mcm.backend.app.database.models.users.Admin;
import com.mcm.backend.app.database.models.users.User;
import com.mcm.backend.app.api.utils.annotations.ValidatedBody;
import com.mcm.backend.exceptions.JsonErrorResponseException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

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
     * Create a new user using a validated request body.
     *
     * @param user The user to create
     * @return Created user
     *
     */
    @PostMapping
    @RequireRole(Admin.class)
    public ResponseEntity<User> createUser(@ValidatedBody(User.class) User user, @CurrentUser Admin admin) throws JsonErrorResponseException {
        try (DAO<User, UUID> userDAO = DAOFactory.createDAO(User.class)) {

            // Check if the username is unique
            if (usernameInUse(userDAO, user)) {
                throw new JsonErrorResponseException("username " + user.getUsername() + " already in use");
            }

            userDAO.add(user);
        }

        return ResponseEntity.ok(user);
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
    public ResponseEntity<?> updateUser(@PathVariable UUID id, @ValidatedBody(User.class) User user) throws JsonErrorResponseException {
        try (DAO<User, UUID> userDAO = DAOFactory.createDAO(User.class)) {

            // Ensure the ID from the path matches the one in the request
            if (!user.getId().equals(id)) {
                throw new JsonErrorResponseException("ID in path and body must match");
            }

            User oldUser = userDAO.get(id);

            if (oldUser == null) {
                return ResponseEntity.notFound().build();
            }

            // If username changed
            if (!oldUser.getUsername().equals(user.getUsername())) {
                // Check if the username is unique
                if (usernameInUse(userDAO, user)) {
                    throw new JsonErrorResponseException("Username already in use");
                }
            }

            userDAO.update(user);
            return ResponseEntity.ok(user);
        }
    }

    /**
     * Delete a user by ID.
     *
     * @param id the user's UUID
     */
    @DeleteMapping("/{id}")
    @RequireRole(Admin.class)
    public ResponseEntity<?> deleteUser(@PathVariable UUID id) {
        try (DAO<User, UUID> userDAO = DAOFactory.createDAO(User.class)) {
            if (userDAO.existsByPrimaryKey(id)) {
                userDAO.delete(id);
                return ResponseEntity.ok(Map.of("message", "Deleted", "id", id.toString()));
            } else {
                return ResponseEntity.notFound().build();
            }
        }
    }

    /**
     * Helper method to check if username is in use
     * @param userDAO The DAO used to fetch for the username
     * @param user The User object which name will be checked for uniqueness
     * @return {@code true} if the username is in use, {@code false} if not.
     */
    private boolean usernameInUse(DAO<User, UUID> userDAO, User user) {
        try {
            return !userDAO.get(User.class.getDeclaredField("username"), user.getUsername()).isEmpty();
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}
