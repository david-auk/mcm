package com.mcm.backend.app.database.models.server;

import com.mcm.backend.app.api.utils.process.ProcessStatus;
import com.mcm.backend.app.database.core.annotations.table.*;
import com.mcm.backend.app.database.core.components.tables.TableEntity;
import com.mcm.backend.app.database.models.server.utils.ServerInitializerUtil;
import com.mcm.backend.app.database.models.server.utils.TmuxUtil;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@TableName("server_instances")
public class ServerInstance implements TableEntity {

    @PrimaryKey(UUID.class)
    private final UUID id;

    @UniqueField
    @TableField(type = String.class)
    private String name;

    @Nullable
    @TableField(type = String.class)
    private String description;

    @TableField(type = String.class, name = "minecraft_version")
    private final String minecraftVersion;

    @TableField(type = String.class, name = "jar_url")
    private final String jarUrl;

    @TableField(type = Boolean.class, name = "eula_accepted")
    private Boolean eulaAccepted;

    @Nullable // Will be generated within constructor
    @TableField(type = Timestamp.class, name = "created_at")
    private final Timestamp createdAt;

    @TableField(type = Integer.class, name = "allocated_ram_mb")
    private Integer allocatedRamMB;

    @TableField(type = Integer.class)
    private Integer port;

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

    public void stop() throws RuntimeException {
        TmuxUtil.stopServerInstance(this);
    }

    public void restart() throws RuntimeException, InterruptedException {
        if (isRunning()) {
            stop();
        } // TODO Implement wait for complete down
        start();
    }

    public List<ServerInstanceProperty> initialize(ProcessStatus ps) throws RuntimeException, IOException, InterruptedException {
        if (eulaAccepted) {
            throw new RuntimeException("Eula already accepted; Already initialized.");
        }
        return ServerInitializerUtil.initialize(this, ps);
    }

    public String sendCommand(String command) throws RuntimeException {
        return null;
    }
}
