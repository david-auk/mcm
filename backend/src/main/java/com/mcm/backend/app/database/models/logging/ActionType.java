package com.mcm.backend.app.database.models.logging;

/**
 * Enum as opposed to an TableEntity. this is because we never want to manipulate ActionType data, just reference it
 */
public enum ActionType {

    // TODO Add CRUD actions

    CHANGE_ALLOCATED_RAM,
    EDIT_CONFIG_FILE,
    UPLOAD_FILE,

    // TODO improve
    EXECUTE_COMMAND,
    ASSIGN_ROLE,
    PROMOTE_ADMIN,
    DEMOTE_ADMIN,

    ;


    private final String text;

    ActionType() {
        this.text = this.name().toLowerCase();
    }

    @Override
    public String toString() {
        return this.text;
    }

}
