package com.mcm.backend.app.api.controllers.users.user;

import com.mcm.backend.app.database.core.components.daos.DAO;
import com.mcm.backend.app.database.models.users.User;

import java.util.UUID;

public class Utils {
    /**
     * Helper method to check if username is in use
     * @param userDAO The DAO used to fetch for the username
     * @param user The User object which name will be checked for uniqueness
     * @return {@code true} if the username is in use, {@code false} if not.
     */
    public static boolean usernameInUse(DAO<User, UUID> userDAO, User user) {
        return usernameInUse(userDAO, user.getUsername());
    }

    /**
     * Helper method to check if username is in use
     * @param userDAO The DAO used to fetch for the username
     * @param username The Username that will be checked for uniqueness
     * @return {@code true} if the username is in use, {@code false} if not.
     */
    public static boolean usernameInUse(DAO<User, UUID> userDAO, String username) {
        try {
            return !userDAO.get(User.class.getDeclaredField("username"), username).isEmpty();
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}
