package com.mcm.backend.app;

import com.mcm.backend.app.api.utils.PasswordHashUtil;
import com.mcm.backend.app.database.core.components.Database;
import com.mcm.backend.app.database.core.components.daos.DAO;
import com.mcm.backend.app.database.core.components.daos.querying.QueryBuilder;
import com.mcm.backend.app.database.core.components.tables.TableEntity;
import com.mcm.backend.app.database.core.factories.DAOFactory;
import com.mcm.backend.app.database.models.users.Admin;
import com.mcm.backend.app.database.models.users.User;
import org.reflections.Reflections;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class InitializeUtil {

    public static void initialize() {
        validateEntities();
        try {
            initializeUser();
        } catch (SQLException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Scan the classpath under the given base package for all TableEntity implementations
     * and validate each one.
     */
    private static void validateEntities() {
        // Adjust the base package to cover your entity classes
        Reflections reflections = new Reflections("com.mcm.backend.app.database.models");
        Set<Class<? extends TableEntity>> entities = reflections.getSubTypesOf(TableEntity.class);
        for (Class<? extends TableEntity> entityClass : entities) {
            TableEntity.validateEntity(entityClass);
        }
    }

    public static void initializeUser() throws SQLException, NoSuchFieldException {
        try (Connection connection = Database.getConnection();
             DAO<User, UUID> userDAO = DAOFactory.createDAO(connection, User.class);
             DAO<Admin, UUID> adminDAO = DAOFactory.createDAO(connection, Admin.class)) {
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
