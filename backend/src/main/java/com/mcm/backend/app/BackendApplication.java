package com.mcm.backend.app;

//import com.mcm.backend.app.database.core.components.daos.DAO;
//import com.mcm.backend.app.database.core.components.daos.DAOInterface;
//import com.mcm.backend.app.database.core.factories.DAOFactory;
//import com.mcm.backend.app.database.models.User;
import com.mcm.backend.app.database.core.components.daos.DAO;
import com.mcm.backend.app.database.core.factories.DAOFactory;
import com.mcm.backend.app.database.models.server.ServerInstance;
import com.mcm.backend.app.database.models.server.ServerInstanceProperty;
import com.mcm.backend.app.database.models.users.Admin;
import com.mcm.backend.app.database.models.users.User;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@SpringBootApplication
public class BackendApplication {




	public static void main(String[] args) {


		ServerInstance serverInstance = new ServerInstance(
				null,
				"Test World",
				"The world ive created for testing",
				"1.21.4",
				"https://api.papermc.io/v2/projects/paper/versions/1.21.4/builds/232/downloads/paper-1.21.4-232.jar",
				false,
				null,
				2048,
				3001
		);

        try {
			List<ServerInstanceProperty> properties = serverInstance.initialize();
			for (ServerInstanceProperty property : properties) {
				System.out.println(property);
			}
        } catch (IOException e) {
			System.out.println("IOException initializing server instance: " + e.getMessage());
        } catch (InterruptedException e) {
			System.out.println("InterruptedException initializing server instance: " + e.getMessage());
        }

		SpringApplication.run(BackendApplication.class, args);
	}

}
