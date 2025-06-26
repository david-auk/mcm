package com.mcm.backend.app.database.models.roles;

import com.mcm.backend.app.database.core.annotations.table.PrimaryKey;
import com.mcm.backend.app.database.core.annotations.table.TableConstructor;
import com.mcm.backend.app.database.core.annotations.table.TableField;
import com.mcm.backend.app.database.core.annotations.table.TableName;
import com.mcm.backend.app.database.core.components.tables.TableEntity;

@TableName("role_inheritance")
public record RoleInheritance(@TableField(name = "role_name", type = String.class) String roleName,
                              @TableField(name = "inherits_role_name", type = String.class) String inheritsRoleName) implements TableEntity {
    @TableConstructor
    public RoleInheritance(String roleName, String inheritsRoleName) {
        if (Role.isValidRole(roleName)) {
            this.roleName = roleName;
        } else throw new IllegalArgumentException("Invalid role name: " + roleName);
        if (Role.isValidRole(inheritsRoleName)) {
            this.inheritsRoleName = inheritsRoleName;
        } else throw new IllegalArgumentException("Invalid role name: " + inheritsRoleName);
    }

    @PrimaryKey(String.class)
    public String getPrimaryKey() {
        return roleName + inheritsRoleName;
    }
}
