package com.mcm.backend.app.database.models.server.utils.rcon;

import com.mcm.backend.app.database.core.components.daos.DAO;
import com.mcm.backend.app.database.core.components.daos.querying.QueryBuilder;
import com.mcm.backend.app.database.core.factories.DAOFactory;
import com.mcm.backend.app.database.models.server.ServerInstance;
import com.mcm.backend.app.database.models.server.ServerInstanceProperty;

import java.util.UUID;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RconUtils {
    // Cache of RCON clients per server to avoid repeated DB lookups
    private static final Map<UUID, RconClient> RCON_CLIENT_CACHE = new ConcurrentHashMap<>();

    public static RconClient buildRconClient(ServerInstance serverInstance) {
        UUID id = serverInstance.getId();
        // Return a cached client if already built
        RconClient cached = RCON_CLIENT_CACHE.get(id);
        if (cached != null) {
            return cached;
        }
        // Ensure EULA accepted before initializing
        if (serverInstance.getEulaAccepted() == null || !serverInstance.getEulaAccepted()) {
            throw new RuntimeException("Cannot initialize RCON client before EULA is accepted");
        }
        try (DAO<ServerInstanceProperty, UUID> sipDAO = DAOFactory.createDAO(ServerInstanceProperty.class)) {
            RconClient newClient = new RconClient(
                    getRconPort(serverInstance, sipDAO),
                    getRconPassword(serverInstance, sipDAO)
            );
            // Cache for future calls
            RCON_CLIENT_CACHE.put(id, newClient);
            return newClient;
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private static int getRconPort(ServerInstance serverInstance, DAO<ServerInstanceProperty, UUID> sipDAO) throws NoSuchFieldException {
        return Integer.parseInt(new QueryBuilder<>(sipDAO)
                .where(ServerInstanceProperty.class.getDeclaredField("serverInstanceId"), serverInstance.getId())
                .and(ServerInstanceProperty.class.getDeclaredField("key"), "rcon.port")
                .getUnique()
                .getValue());
    }

    private static String getRconPassword(ServerInstance serverInstance, DAO<ServerInstanceProperty, UUID> sipDAO) throws NoSuchFieldException {
        return new QueryBuilder<>(sipDAO)
                .where(ServerInstanceProperty.class.getDeclaredField("serverInstanceId"), serverInstance.getId())
                .and(ServerInstanceProperty.class.getDeclaredField("key"), "rcon.password")
                .getUnique()
                .getValue();
    }

}
