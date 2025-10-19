package com.mcm.backend.app.database.models.logging;

import io.github.david.auk.fluid.jdbc.annotations.table.PrimaryKey;
import io.github.david.auk.fluid.jdbc.annotations.table.TableConstructor;
import io.github.david.auk.fluid.jdbc.annotations.table.TableColumn;
import io.github.david.auk.fluid.jdbc.annotations.table.TableName;
import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;

@TableName("action_types")
public class ActionTypeEntity implements TableEntity {

    @TableColumn
    @PrimaryKey
    private final String name;

    @TableColumn(name = "message_template")
    private final String messageTemplate;

    @TableColumn
    private final String severity;

    @TableConstructor
    public ActionTypeEntity(String name, String messageTemplate, String severity) {
        this.name = name;
        this.messageTemplate = messageTemplate;
        this.severity = severity;
    }

    public String getName() {
        return name;
    }

    public String getMessageTemplate() {
        return messageTemplate;
    }

    public String getSeverity() {
        return severity;
    }
}
