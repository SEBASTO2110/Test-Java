package com.littlehorizons.erp.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Singleton that keeps one connection to the MySQL database for the whole application.
 * The credentials are read from the db.properties file in the project folder.
 */
public class DBConnectionSingleton {
    private static final String CONFIG_FILE = "db.properties";
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/erp_little_horizons"
            + "?useSSL=false&serverTimezone=America/Bogota&allowPublicKeyRetrieval=true&characterEncoding=UTF-8";

    private static Connection connection = null;

    private DBConnectionSingleton() {
    }

    public static Connection getInstance() {
        try {
            if (connection == null || connection.isClosed()) {
                Properties config = loadConfig();
                connection = DriverManager.getConnection(
                        config.getProperty("db.url", DEFAULT_URL),
                        config.getProperty("db.user", "root"),
                        config.getProperty("db.password", ""));
            }
        } catch (SQLException e) {
            ErrorLogger.log("Database connection", e);
            throw new RuntimeException("No fue posible conectarse a la base de datos. " + e.getMessage());
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
            } catch (SQLException e) {
                ErrorLogger.log("Closing database connection", e);
            }
        }
    }

    private static Properties loadConfig() {
        Properties config = new Properties();
        try (FileInputStream input = new FileInputStream(CONFIG_FILE)) {
            config.load(input);
        } catch (IOException e) {
            ErrorLogger.log("File " + CONFIG_FILE + " not found, using default values", e);
        }
        return config;
    }
}
