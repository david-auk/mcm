package com.mcm.backend.app.database.models.roles;

import com.mcm.backend.app.database.core.annotations.table.PrimaryKey;
import com.mcm.backend.app.database.core.annotations.table.TableConstructor;
import com.mcm.backend.app.database.core.annotations.table.TableColumn;
import com.mcm.backend.app.database.core.annotations.table.TableName;
import com.mcm.backend.app.database.core.components.tables.TableEntity;

@TableName("roles")
public record RoleEntity(@PrimaryKey @TableColumn String name,
                         @TableColumn String description) implements TableEntity {

    @TableConstructor
    public RoleEntity(String name, String description) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        } else if (!Role.isValidRole(name)) {
            throw new IllegalArgumentException("Invalid role name");
        }
        this.name = name;
        if (description == null || description.isEmpty()) {
            throw new IllegalArgumentException("Description cannot be null or empty");
        }
        this.description = description;
    }

    @Override
    public String name() {
        return name;
    }
}
