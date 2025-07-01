package com.mcm.backend.app.api.controllers.serverinstances.initialize;

import com.mcm.backend.app.api.utils.process.LogEntry;
import com.mcm.backend.app.api.utils.process.ProcessRegistry;
import com.mcm.backend.app.api.utils.process.ProcessState;
import com.mcm.backend.app.api.utils.process.ProcessStatus;
import com.mcm.backend.app.database.core.components.daos.DAO;
import com.mcm.backend.app.database.core.factories.DAOFactory;
import com.mcm.backend.app.database.models.server.ServerInstance;
import com.mcm.backend.app.database.models.server.ServerInstanceProperty;
import com.mcm.backend.app.database.models.server.utils.ServerCoreUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public class InitService {

    private final ProcessRegistry registry;

    @Autowired
    public InitService(ProcessRegistry registry) {
        this.registry = registry;
    }

    @Async("initExecutor")
    public void doInitialization(ServerInstance serverInstance, UUID processId) {
        ProcessStatus ps = registry.get(processId);

        try {
            // Build serverInstance
            ps.getLogs().add(new LogEntry("→ building instance"));
            List<ServerInstanceProperty> properties = serverInstance.initialize(ps);

            // Update EULA Accepted in DB
            ps.getLogs().add(new LogEntry("→ updating initialization status to DB"));
            try (DAO<ServerInstance, UUID> serverInstanceDAO = DAOFactory.createDAO(ServerInstance.class)) {
                serverInstanceDAO.update(serverInstance);
            }

            // Save properties
            ps.getLogs().add(new LogEntry("→ saving properties to DB"));
            try (DAO<ServerInstanceProperty, UUID> serverInstancePropertyDAO = DAOFactory.createDAO(ServerInstanceProperty.class)) {
                for (var property : properties) {
                    serverInstancePropertyDAO.add(property);
                }
            }

            // Complete initialization
            ps.getLogs().add(new LogEntry("Initialization complete"));
            ps.setState(ProcessState.SUCCESS);

            // Log
            // TODO LOG

        } catch (Exception e) {
            ps.getLogs().add(new LogEntry("✖ Error: " + e.getMessage()));

            ps.getLogs().add(new LogEntry("→ Cleaning files"));
            try {
                ServerCoreUtil.cleanServerInstance(serverInstance);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            ps.setState(ProcessState.ERROR);
        }
    }
}
