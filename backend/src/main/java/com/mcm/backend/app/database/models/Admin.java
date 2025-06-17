package com.mcm.backend.database.models;

import com.mcm.backend.database.core.annotations.table.Id;
import com.mcm.backend.database.core.annotations.table.TableConstructor;
import com.mcm.backend.database.core.annotations.table.TableName;
import com.mcm.backend.database.core.components.tables.AutoTableEntity;

import java.util.UUID;

@TableName("admins")
public class Admin implements AutoTableEntity {

    @Id(UUID.class)
    private final UUID id;

    @TableConstructor
    public Admin(UUID uuid) {
        this.id = uuid;
    }

    public Admin(User user) {
        this.id = user.getId();
    }

    public UUID getId() {
        return id;
    }
}

