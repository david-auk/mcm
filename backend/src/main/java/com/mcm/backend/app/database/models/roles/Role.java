package com.mcm.backend.app.database.models.roles;

public enum Role {

    USER,
    VIEWER,
    OPERATOR,
    EDITOR,
    MAINTAINER;

    private final String text;

    Role() {
        this.text = this.name().toLowerCase();
    }

    @Override
    public String toString() {
        return this.text;
    }

    public static boolean isValidRole(String role) {
        if (role == null) return false;
        try {
            Role.valueOf(role.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
