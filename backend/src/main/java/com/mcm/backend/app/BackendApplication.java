package com.mcm.backend.app;

//import com.mcm.backend.app.database.core.components.daos.DAO;
//import com.mcm.backend.app.database.core.components.daos.DAOInterface;
//import com.mcm.backend.app.database.core.factories.DAOFactory;
//import com.mcm.backend.app.database.models.User;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.UUID;

@SpringBootApplication
public class BackendApplication {




	public static void main(String[] args) {

//		try (DAO<User, UUID> userDAO = DAOFactory.createDAO(User.class)) {
//			//userDAO.close();
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
		SpringApplication.run(BackendApplication.class, args);
	}

}
