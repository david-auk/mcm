package com.mcm.backend.app.database.models.server.backups;

import com.mcm.backend.app.database.core.annotations.table.*;
import com.mcm.backend.app.database.core.components.tables.TableEntity;
import com.mcm.backend.app.database.models.server.ServerInstance;
import com.mcm.backend.app.database.models.server.backups.utils.BackupUtil;
import com.mcm.backend.app.database.models.users.User;

import java.nio.file.Path;
import java.sql.Timestamp;
import java.util.Objects;
import java.util.UUID;

@TableName("backups")
public record Backup(@PrimaryKey @TableColumn UUID id,
                     @TableColumn(name = "server_instance_id") @ForeignKey ServerInstance serverInstance,
                     @TableColumn(name = "created_by") @ForeignKey User createdBy,
                     @Nullable @TableColumn Timestamp timestamp) implements TableEntity {

    @TableConstructor
    public Backup(UUID id, ServerInstance serverInstance, User createdBy, Timestamp timestamp) {
        this.id = Objects.requireNonNullElseGet(id, UUID::randomUUID);
        if (serverInstance == null) {
            throw new IllegalArgumentException("serverInstance cannot be null");
        }
        this.serverInstance = serverInstance;
        if (createdBy == null) {
            throw new IllegalArgumentException("Created by cannot be null");
        }
        this.createdBy = createdBy;
        this.timestamp = Objects.requireNonNullElseGet(timestamp, () -> new Timestamp(System.currentTimeMillis()));
    }

    public Backup(ServerInstance serverInstance, User createdBy) {
        this(null, serverInstance, createdBy, null);
    }

    // Get Root path
    public Path getBackupRoot() {
        return getBackupRoot(serverInstance);
    }
    public static Path getBackupRoot(ServerInstance serverInstance) {
        return getBackupRoot(serverInstance.getPath());
    }
    public static Path getBackupRoot(Path serverInstancePath) {
        return serverInstancePath.resolve("backups");
    }

    public Path getPath() {
        return getBackupRoot().resolve(id.toString());
    }
    public static Path getPath(Path serverInstancePath, UUID backupInstanceId) {
        return getBackupRoot(serverInstancePath).resolve(backupInstanceId.toString());
    }

    public void write() {
        BackupUtil.write(this);
    }

    public void restore() {
        BackupUtil.restore(this);
    }
}
