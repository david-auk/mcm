package com.mcm.backend.app.database.models.roles;

import io.github.david.auk.fluid.jdbc.annotations.table.PrimaryKey;
import io.github.david.auk.fluid.jdbc.annotations.table.TableConstructor;
import io.github.david.auk.fluid.jdbc.annotations.table.TableColumn;
import io.github.david.auk.fluid.jdbc.annotations.table.TableName;
import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;

@TableName("role_inheritance")
public record RoleInheritance(@TableColumn(name = "role_name") String roleName,
                              @TableColumn(name = "inherits_role_name") String inheritsRoleName) implements TableEntity {
    @TableConstructor
    public RoleInheritance(String roleName, String inheritsRoleName) {
        if (Role.isValidRole(roleName)) {
            this.roleName = roleName;
        } else throw new IllegalArgumentException("Invalid role name: " + roleName);
        if (Role.isValidRole(inheritsRoleName)) {
            this.inheritsRoleName = inheritsRoleName;
        } else throw new IllegalArgumentException("Invalid role name: " + inheritsRoleName);
    }

    @PrimaryKey
    public String getPrimaryKey() {
        return roleName + inheritsRoleName;
    }
}
