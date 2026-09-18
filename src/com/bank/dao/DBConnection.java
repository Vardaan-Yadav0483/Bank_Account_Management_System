package com.bank.dao;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Course: CSE2006 - Programming in Java
 * Module 5: Database Applications with JDBC
 * 
 * Manages database connectivity using the JDBC driver and initializes required relational tables.
 * Uses portable SQLite so zero external server configuration is needed.
 */
public class DBConnection {
    private static final String DB_DIR = "data";
    private static final String DB_FILE = "data" + File.separator + "bank.db";
    private static final String JDBC_URL = "jdbc:sqlite:" + DB_FILE;

    static {
        try {
            // Load the SQLite JDBC driver class
            Class.forName("org.sqlite.JDBC");
            // Ensure data directory exists
            File dir = new File(DB_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            // Initialize schema
            initializeDatabase();
        } catch (ClassNotFoundException e) {
            System.err.println("[JDBC Warning] SQLite JDBC driver not found on classpath!");
            System.err.println("Please run using the provided LAUNCH.bat or specify -cp \"lib/*\"");
        } catch (Exception e) {
            System.err.println("[Database Init Error] " + e.getMessage());
        }
    }

    /**
     * Obtains a live JDBC connection to the SQLite database.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL);
    }

    /**
     * Creates relational tables if they do not already exist and seeds initial demo records.
     */
    public static void initializeDatabase() {
        String createAccountsTable = 
            "CREATE TABLE IF NOT EXISTS accounts (" +
            "  account_no TEXT PRIMARY KEY, " +
            "  holder_name TEXT NOT NULL, " +
            "  account_type TEXT NOT NULL, " +
            "  balance REAL NOT NULL, " +
            "  extra_param REAL NOT NULL, " +
            "  status TEXT NOT NULL, " +
            "  created_at TEXT NOT NULL" +
            ");";

        String createTransactionsTable = 
            "CREATE TABLE IF NOT EXISTS transactions (" +
            "  trans_id TEXT PRIMARY KEY, " +
            "  account_no TEXT NOT NULL, " +
            "  trans_type TEXT NOT NULL, " +
            "  amount REAL NOT NULL, " +
            "  balance_after REAL NOT NULL, " +
            "  timestamp TEXT NOT NULL, " +
            "  remarks TEXT, " +
            "  FOREIGN KEY (account_no) REFERENCES accounts(account_no)" +
            ");";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.execute(createAccountsTable);
            stmt.execute(createTransactionsTable);

            // Seed initial records if empty
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM accounts;")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    seedDemoData(stmt);
                }
            }
        } catch (SQLException e) {
            System.err.println("[Database Schema Setup Error] " + e.getMessage());
        }
    }

    private static void seedDemoData(Statement stmt) throws SQLException {
        stmt.executeUpdate(
            "INSERT INTO accounts (account_no, holder_name, account_type, balance, extra_param, status, created_at) " +
            "VALUES ('SB1001', 'Ankit Pathak', 'SAVINGS', 15000.00, 4.50, 'ACTIVE', datetime('now', 'localtime'));"
        );
        stmt.executeUpdate(
            "INSERT INTO transactions (trans_id, account_no, trans_type, amount, balance_after, timestamp, remarks) " +
            "VALUES ('TXN100001', 'SB1001', 'DEPOSIT', 15000.00, 15000.00, datetime('now'), 'Initial Opening Deposit');"
        );
    }
}
