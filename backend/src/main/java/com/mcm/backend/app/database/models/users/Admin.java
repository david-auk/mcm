package com.mcm.backend.app.database.models.users;

import com.mcm.backend.app.database.core.annotations.table.*;
import com.mcm.backend.app.database.core.components.tables.TableEntity;

import java.util.UUID;

@TableName("admins")
public class Admin implements TableEntity {

    @PrimaryKey
    @ForeignKey // References the @PrimaryKey of User.class
    @TableColumn(name = "id")
    private final User user;

    @TableConstructor
    public Admin(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public UUID getId() {
        return user.getId();
    }

    @Override
    public String toString() {
        return "{User: " + user + "}";
    }
}

