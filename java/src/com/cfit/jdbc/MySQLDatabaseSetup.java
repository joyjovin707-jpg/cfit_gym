package com.cfit.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * Standalone MySQL Database Setup Utility:
 * Creates the 'cfit_db' database and initializes all tables and seed records directly via JDBC.
 * Run directly with:
 *   java -cp "java/bin:lib/*" com.cfit.jdbc.MySQLDatabaseSetup
 */
public class MySQLDatabaseSetup {

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("   C-FIT SYSTEM — MYSQL DATABASE SCHEMA SETUP UTILITY");
        System.out.println("================================================================================");

        String host = JDBCConfig.getDbHost();
        String port = JDBCConfig.getDbPort();
        String user = JDBCConfig.getDbUser();
        String pass = JDBCConfig.getDbPass();
        String dbName = JDBCConfig.getDbName();

        // 1. Connect to MySQL server without database first to ensure cfit_db exists
        String serverUrl = "jdbc:mysql://" + host + ":" + port + "/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        System.out.println("[INFO] Connecting to MySQL server at " + host + ":" + port + "...");

        try {
            Class.forName(ConnectionFactory.MYSQL_CJ_DRIVER);
        } catch (Exception e) {
            try {
                Class.forName(ConnectionFactory.MYSQL_LEGACY_DRIVER);
            } catch (Exception ex) {
                System.err.println("[ERROR] MySQL Driver not found. Please ensure 'lib/mysql-connector-j-8.3.0.jar' is on classpath.");
                return;
            }
        }

        try (Connection serverConn = DriverManager.getConnection(serverUrl, user, pass);
             Statement stmt = serverConn.createStatement()) {

            System.out.println("[INFO] Creating database '" + dbName + "' if not exists...");
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + dbName + " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            System.out.println("  ✓ Database '" + dbName + "' verified/created successfully.");

        } catch (Exception e) {
            System.err.println("[WARN] Could not create database directly on root connection: " + e.getMessage());
            System.err.println("Attempting connection to '" + dbName + "' directly...");
        }

        // 2. Connect to cfit_db and create tables
        try (Connection dbConn = ConnectionFactory.createMySQLConnection()) {
            System.out.println("[INFO] Connected to database '" + dbName + "'. Initializing tables and seed data...");
            DatabaseInitializer.initializeSchema(dbConn);
            System.out.println("  ✓ All 8 tables (admin, staff_users, trainers, members, attendance, payments, classes, class_bookings) ready!");
            System.out.println("\n[SUCCESS] MySQL Database setup completed successfully.");
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to initialize tables: " + e.getMessage());
        }
    }
}
