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

		// Initialize
		InitializeUtil.initialize();

		// Run Spring Boot
		SpringApplication.run(BackendApplication.class, args);
	}
}
