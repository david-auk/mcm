package com.mcm.backend.app.database.models.server;

import com.mcm.backend.app.database.core.annotations.table.PrimaryKey;
import com.mcm.backend.app.database.core.annotations.table.TableConstructor;
import com.mcm.backend.app.database.core.annotations.table.TableColumn;
import com.mcm.backend.app.database.core.annotations.table.TableName;
import com.mcm.backend.app.database.core.components.tables.TableEntity;

import java.util.Objects;
import java.util.UUID;

@TableName("server_instance_properties")
public class ServerInstanceProperty implements TableEntity {

    @TableColumn
    @PrimaryKey
    private final UUID id;

    @TableColumn(name = "server_instance_id")
    private final UUID serverInstanceId;

    @TableColumn
    private Boolean hidden;

    @TableColumn
    private String type;

    @TableColumn
    private String value;

    @TableColumn
    private final String key;


    @TableConstructor
    public ServerInstanceProperty(UUID id, UUID serverInstanceId, Boolean hidden, String type, String value, String key) {
        this.id = Objects.requireNonNullElseGet(id, UUID::randomUUID);
        this.serverInstanceId = serverInstanceId;
        this.hidden = hidden;
        this.type = type;
        this.value = value;
        this.key = key;
    }

    //- Getters & Setters

    public UUID getId() {
        return id;
    }

    public UUID getServerInstanceId() {
        return serverInstanceId;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean isHidden() {
        return hidden;
    }

    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }

    @Override
    public String toString() {
        return "{id: " + id + ", hidden: " + hidden + ", key: " + key + ", value: " + value + ", type: " + type + "}";
    }
}