//package com.mcm.backend.app.database.models.users;
//
//import com.mcm.backend.app.database.core.annotations.table.TableConstructor;
//import com.mcm.backend.app.database.core.components.tables.AutoTableEntity;
//import com.mcm.backend.app.database.core.annotations.table.Id;
//import com.mcm.backend.app.database.core.annotations.table.TableField;
//import com.mcm.backend.app.database.core.annotations.table.TableName;
//
//import java.util.Objects;
//import java.util.UUID;
//
//@TableName("action_types")
//public class ActionType implements AutoTableEntity {
//
//    public static Enum<String> SEVERITY = new Enum<String>() {
//    }
//
//    @Id(UUID.class)
//    private final UUID id;
//
//    //@UniqueField TODO Implement
//    // DAO.getByVal(ActionType.name, "Value")
//    @TableField(type = String.class)
//    private String name;
//
//    @TableField(type = String.class)
//    private String description;
//
//    @TableConstructor
//    public ActionType(UUID id, String name, String description) {
//        this.id = Objects.requireNonNullElseGet(id, UUID::randomUUID);
//        this.name = name;
//        this.description = description;
//    }
//
//}
