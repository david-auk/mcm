package com.mcm.backend.app.database.models.logging;

/**
 * Enum as opposed to an TableEntity. this is because we never want to manipulate ActionType data, just reference it
 */
public enum ActionType {

    // User operations
    USER_CHANGED_USERNAME,
    USER_CHANGED_PASSWORD,

    // Operator operations
    EXECUTE_COMMAND,
    START_SERVER,
    STOP_SERVER,

    // Editor operations
    EDIT_PROPERTY,
    UPLOAD_FILE,
    CHANGE_FILE,
    DELETE_FILE,

    // Maintainer operations
    CHANGE_ALLOCATED_RAM,

    // Admin operations
    ASSIGN_ROLE,
    USER_PROMOTE,
    ADMIN_DEMOTE,

        // User management
        USER_CREATE,
        USER_UPDATE,
        USER_DELETE,

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
