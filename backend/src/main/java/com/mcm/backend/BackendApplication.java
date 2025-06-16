package com.mcm.backend;

import com.mcm.backend.database.core.components.DAOInterface;
import com.mcm.backend.database.core.factories.DAOFactory;
import com.mcm.backend.database.models.User;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.UUID;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {

		try (DAOInterface<User, UUID> userDao = DAOFactory.createDAO(User.class)) {
			userDao.exists;
		} catch (Exception e) {
            throw new RuntimeException(e);
        }

        //SpringApplication.run(BackendApplication.class, args);
	}

}
