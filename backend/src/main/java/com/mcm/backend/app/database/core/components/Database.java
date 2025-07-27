package com.mcm.backend.app.database.core.components;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

    private static final String URL = System.getenv("DATASOURCE_URL");
    private static final String USER = System.getenv("DATASOURCE_USERNAME");
    private static final String PASSWORD = System.getenv("DATASOURCE_PASSWORD");

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
