package com.mcm.backend.app;

//import com.mcm.backend.app.database.core.components.daos.DAO;
//import com.mcm.backend.app.database.core.components.daos.DAOInterface;
//import com.mcm.backend.app.database.core.factories.DAOFactory;
//import com.mcm.backend.app.database.models.User;
import com.mcm.backend.app.database.core.components.daos.DAO;
import com.mcm.backend.app.database.core.factories.DAOFactory;
import com.mcm.backend.app.database.models.server.ServerInstance;
import com.mcm.backend.app.database.models.server.ServerInstanceProperty;
import com.mcm.backend.app.database.models.server.utils.ServerCoreUtil;
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

//		try (DAO<ServerInstance, UUID> serverInstanceDAO = DAOFactory.createDAO(ServerInstance.class)){
//
//			for (ServerInstance serverInstance : serverInstanceDAO.getAll()) {
//				serverInstance.start();
//			}
//
//			ServerInstance serverInstance = new ServerInstance(
//					null,
//					"Test World1",
//					"The world ive created for testing",
//					"1.21.4",
//					"https://api.papermc.io/v2/projects/paper/versions/1.21.4/builds/232/downloads/paper-1.21.4-232.jar",
//					false,
//					null,
//					2048,
//					3002
//			);
//
//			try (DAO<ServerInstanceProperty, UUID> serverInstancePropertyDAO = DAOFactory.createDAO(ServerInstanceProperty.class)) {
//				List<ServerInstanceProperty> properties = serverInstance.initialize();
//
//				// Save Initialized serverInstance to DB
//				serverInstanceDAO.add(serverInstance);
//
//				for (ServerInstanceProperty property : properties) {
//					serverInstancePropertyDAO.add(property);
//				}
//
//				serverInstance.start();
//
//			} catch (Exception e) {
//				System.out.println("Something went wrong, Cleaning up...");
//				System.out.println(e.getMessage());
//				// Remove record
//				if (serverInstanceDAO.exists(serverInstance)) {
//					serverInstanceDAO.delete(serverInstance.getId());
//				}
//				try {
//					// Remove files
//					ServerCoreUtil.cleanServerInstance(serverInstance);
//				} catch (IOException ex) {
//					System.out.println("IOException cleaning server instance: " + ex.getMessage());
//				}
//				if (e instanceof IOException) {
//					System.out.println("IOException initializing server instance: " + e.getMessage());
//				} else if (e instanceof InterruptedException) {
//					System.out.println("InterruptedException initializing server instance: " + e.getMessage());
//				}
//			}
//		}
//
		SpringApplication.run(BackendApplication.class, args);
	}
}
