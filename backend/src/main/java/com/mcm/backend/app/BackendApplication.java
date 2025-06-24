package com.mcm.backend.app;

import com.mcm.backend.app.api.utils.PasswordHashUtil;
import com.mcm.backend.app.database.core.components.daos.DAO;
import com.mcm.backend.app.database.core.factories.DAOFactory;
import com.mcm.backend.app.database.models.users.Admin;
import com.mcm.backend.app.database.models.users.User;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;
import java.util.UUID;

@SpringBootApplication
public class BackendApplication {
	public static void main(String[] args) {
		initialize();
		SpringApplication.run(BackendApplication.class, args);
	}

	public static void initialize() {
		try (DAO<User, UUID> userDAO = DAOFactory.createDAO(User.class)) {
			try (DAO<Admin, UUID> adminDAO = DAOFactory.createDAO(Admin.class)) {
				List<Admin> admins = adminDAO.getAll();

				// Check if any admins exist
				if (admins.isEmpty()) {
					// If not create a new default admin user
					User user = new User(
						null,
						"admin",
						PasswordHashUtil.hashPassword(System.getenv("DEFAULT_USER_PASSWORD"))
					);

					// Add the default user to the user table
					userDAO.add(user);

					// Add the default user to the admin table
					adminDAO.add(new Admin(user));
				}

			}
		}
	}
}
