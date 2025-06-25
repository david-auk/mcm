package com.mcm.backend.app.database.models.logging;

import com.mcm.backend.app.database.core.annotations.table.PrimaryKey;
import com.mcm.backend.app.database.core.annotations.table.TableConstructor;
import com.mcm.backend.app.database.core.annotations.table.TableField;
import com.mcm.backend.app.database.core.annotations.table.TableName;
import com.mcm.backend.app.database.core.components.tables.TableEntity;
import org.springframework.data.annotation.Id;

@TableName("action_types")
public class ActionTypeEntity implements TableEntity {
    @PrimaryKey(String.class)
    private final String name;

    @TableField(type = String.class, name = "message_template")
    private final String messageTemplate;

    @TableField(type = String.class)
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
