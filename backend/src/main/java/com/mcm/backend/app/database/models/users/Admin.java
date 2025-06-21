package com.mcm.backend.app.database.models.users;

import com.mcm.backend.app.database.core.annotations.table.PrimaryKey;
import com.mcm.backend.app.database.core.annotations.table.TableConstructor;
import com.mcm.backend.app.database.core.annotations.table.TableName;
import com.mcm.backend.app.database.core.components.tables.TableEntity;

import java.util.UUID;

@TableName("admins")
public class Admin implements TableEntity {

    @PrimaryKey(UUID.class)
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

