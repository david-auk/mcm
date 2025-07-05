package com.mcm.backend.app;

import com.mcm.backend.app.api.utils.PasswordHashUtil;
import com.mcm.backend.app.database.core.components.daos.DAO;
import com.mcm.backend.app.database.core.components.daos.querying.QueryBuilder;
import com.mcm.backend.app.database.core.factories.DAOFactory;
import com.mcm.backend.app.database.models.users.Admin;
import com.mcm.backend.app.database.models.users.User;

import java.util.List;
import java.util.UUID;

public class InitializeUtil {

    public static void initialize() {
        try {
            initializeUser();
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public static void initializeUser() throws NoSuchFieldException {
        try (DAO<User, UUID> userDAO = DAOFactory.createDAO(User.class);
             DAO<Admin, UUID> adminDAO = DAOFactory.createDAO(Admin.class)) {
            List<Admin> admins = adminDAO.getAll();

            // Check if any admins exist
            if (admins.isEmpty()) {

                User userCalledAdmin = new QueryBuilder<>(userDAO)
                        .where(User.class.getDeclaredField("username"), "admin")
                        .getUnique();

                if (userCalledAdmin == null) {

                    userCalledAdmin = new User(
                            null,
                            "admin",
                            PasswordHashUtil.hashPassword(System.getenv("DEFAULT_USER_PASSWORD"))
                    );

                    // Add the default user to the user table
                    userDAO.add(userCalledAdmin);
                }

                // Add the default user to the admin table
                adminDAO.add(new Admin(userCalledAdmin));
            }

        }
    }
}

