package com.mcm.backend.app.database.models.server;

import com.mcm.backend.app.api.utils.process.ProcessStatus;
import com.mcm.backend.app.database.core.annotations.table.*;
import com.mcm.backend.app.database.core.components.daos.DAO;
import com.mcm.backend.app.database.core.components.daos.querying.QueryBuilder;
import com.mcm.backend.app.database.core.components.tables.TableEntity;
import com.mcm.backend.app.database.models.server.utils.ServerCoreUtil;
import com.mcm.backend.app.database.models.server.utils.ServerInitializerUtil;
import com.mcm.backend.app.database.models.server.utils.TmuxUtil;
import com.mcm.backend.app.database.models.server.utils.rcon.RconClient;
import com.mcm.backend.app.database.models.server.utils.rcon.RconUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@TableName("server_instances")
public class ServerInstance implements TableEntity {

    @TableColumn
    @PrimaryKey
    private final UUID id;

    @TableColumn
    @UniqueColumn
    private String name;

    @TableColumn
    @Nullable
    private String description;

    @TableColumn(name = "minecraft_version")
    private final String minecraftVersion;

    @TableColumn(name = "jar_url")
    private final String jarUrl;

    @TableColumn(name = "eula_accepted")
    private Boolean eulaAccepted;

    @TableColumn(name = "created_at")
    @Nullable // Will be generated within constructor
    private final Timestamp createdAt;

    @TableColumn(name = "allocated_ram_mb")
    private Integer allocatedRamMB;

    @TableColumn
    private Integer port;

    // RCON client instance, initialized when the server is initialized
    private RconClient rconClient;

    @TableConstructor
    public ServerInstance(UUID id, String name, String description, String minecraftVersion, String jarUrl, Boolean eulaAccepted, Timestamp createdAt, Integer allocatedRamMB, Integer port) {
        this.id = Objects.requireNonNullElseGet(id, UUID::randomUUID);
        setName(name);
        setDescription(description);
        if (minecraftVersion == null || minecraftVersion.isEmpty()) {
            throw new IllegalArgumentException("MinecraftVersion cannot be null or empty");
        }
        this.minecraftVersion = minecraftVersion;
        if (jarUrl == null || jarUrl.isEmpty()) {
            throw new IllegalArgumentException("JarUrl cannot be null or empty");
        }
        this.jarUrl = jarUrl;
        setEulaAccepted(eulaAccepted);
        if (createdAt == null) {
            this.createdAt = new Timestamp(System.currentTimeMillis());
        } else {
            this.createdAt = new Timestamp(createdAt.getTime()); // New assignment in memory to avoid tinkering
        }
        setAllocatedRamMB(allocatedRamMB);
        setPort(port);
    }

    // - Getters and setters

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMinecraftVersion() {
        if (minecraftVersion == null || minecraftVersion.isEmpty()) {
            throw new IllegalArgumentException("MinecraftVersion cannot be null or empty");
        }
        return minecraftVersion;
    }

    public String getJarUrl() {
        return jarUrl;
    }

    public Boolean getEulaAccepted() {
        return eulaAccepted;
    }

    public void setEulaAccepted(Boolean eulaAccepted) {
        if (eulaAccepted == null) {
            throw new IllegalArgumentException("EulaAccepted cannot be null");
        }
        this.eulaAccepted = eulaAccepted;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public Integer getAllocatedRamMB() {
        return allocatedRamMB;
    }

    public void setAllocatedRamMB(Integer allocatedRamMB) {
        if (allocatedRamMB == null) {
            throw new IllegalArgumentException("AllocatedRamMB cannot be null");
        }
        this.allocatedRamMB = allocatedRamMB;
    }


    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        if (port == null || !(port > 1023 && port < 65535)) {
            throw new IllegalArgumentException("Port must be between 1023 and 65535");
        } else if (!isEven(port)) {
            throw new IllegalArgumentException("Port must be even (so uneven port is reseved for rcon)");
        }
        this.port = port;
    }

    public boolean isEven(int number) {
        return number % 2 == 0;
    }

    // - Logic

    public Boolean isRunning() {
        return TmuxUtil.isTmuxSessionRunning(this);
    }

    public void start() throws RuntimeException {
        TmuxUtil.startServerInstance(this);
    }

    /**
     * Gracefully stops the server by sending the RCON 'stop' command,
     * waiting up to a timeout for the process to exit, then falling back
     * to force-stopping via TMUX if still running.
     */
    public void stop() throws RuntimeException {
        // If server not running, nothing to do
        if (!isRunning()) {
            return;
        }
        // Attempt graceful shutdown via RCON
        try {
            sendCommand("stop");
        } catch (Exception e) {
            throw new RuntimeException("Failed to send stop command via RCON", e);
        }
        // Wait for shutdown up to timeout
        long timeoutMillis = 30_000;
        long startTime = System.currentTimeMillis();
        while (isRunning() && System.currentTimeMillis() - startTime < timeoutMillis) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        // If still running after timeout, force stop via TMUX
        if (isRunning()) {
            TmuxUtil.stopServerInstance(this);
        }
    }

//    public void restart() throws RuntimeException, InterruptedException {
//        if (isRunning()) {
//            stop();
//        } // TODO Implement wait for complete down
//        start();
//    }

    public List<ServerInstanceProperty> initialize(ProcessStatus ps) throws RuntimeException, IOException, InterruptedException {
        if (eulaAccepted) {
            throw new RuntimeException("Eula already accepted; Already initialized.");
        }
        return ServerInitializerUtil.initialize(this, ps);
    }

    public String sendCommand(String command) throws RuntimeException {
        return getRconClient().sendCommand(command);
    }

    private void setRconClient(RconClient rconClient) {
        this.rconClient = rconClient;
    }

    /**
     * Returns the RCON client, initializing it if necessary.
     */
    private RconClient getRconClient() {
        if (rconClient == null) {
            setRconClient(RconUtils.buildRconClient(this));
        }
        return rconClient;
    }

    public Path getPath() {
        return ServerCoreUtil.getServerInstanceDirectory(this);
    }

    public List<ServerInstanceProperty> getProperties(DAO<ServerInstanceProperty, UUID> sipDAO) throws NoSuchFieldException {
        return new QueryBuilder<>(sipDAO)
                .where(ServerInstanceProperty.class.getDeclaredField("serverInstanceId"), id)
                .get();
    }
}
