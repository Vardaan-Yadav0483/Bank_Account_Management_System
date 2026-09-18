package com.bank.dao;

import com.bank.model.Account;
import com.bank.model.CurrentAccount;
import com.bank.model.SavingsAccount;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Course: CSE2006 - Programming in Java
 * Module 5: Database Applications with JDBC (PreparedStatement, ResultSet, CRUD)
 * Module 4: Collections Framework (ArrayList, List)
 * 
 * Data Access Object (DAO) managing persistence lifecycle for Account entities.
 */
public class AccountDAO {
    private static final DateTimeFormatter DT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Persists a new Account (Savings or Current) into the database.
     */
    public boolean saveAccount(Account account) {
        String sql = "INSERT INTO accounts (account_no, holder_name, account_type, balance, extra_param, status, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?);";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, account.getAccountNumber());
            pstmt.setString(2, account.getHolderName());
            pstmt.setString(3, account.getAccountType());
            pstmt.setDouble(4, account.getBalance());
            pstmt.setDouble(5, account.getExtraParameter());
            pstmt.setString(6, account.getStatus());
            pstmt.setString(7, account.getCreatedAt().format(DT_FORMATTER));

            int rows = pstmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("[AccountDAO Error] Failed to save account: " + e.getMessage());
            return false;
        }
    }

    /**
     * Updates an account balance after a financial transaction.
     */
    public boolean updateBalance(String accountNo, double newBalance) {
        String sql = "UPDATE accounts SET balance = ? WHERE account_no = ?;";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDouble(1, newBalance);
            pstmt.setString(2, accountNo);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[AccountDAO Error] Failed to update balance: " + e.getMessage());
            return false;
        }
    }

    /**
     * Updates status (e.g. "ACTIVE" or "CLOSED").
     */
    public boolean updateStatus(String accountNo, String status) {
        String sql = "UPDATE accounts SET status = ? WHERE account_no = ?;";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            pstmt.setString(2, accountNo);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[AccountDAO Error] Failed to update status: " + e.getMessage());
            return false;
        }
    }

    /**
     * Fetches a single Account by its primary key account number.
     * Demonstrates Polymorphic instantiation based on account_type column.
     */
    public Account getAccountByNo(String accountNo) {
        String sql = "SELECT account_no, holder_name, account_type, balance, extra_param, status, created_at " +
                     "FROM accounts WHERE account_no = ?;";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, accountNo);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToAccount(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[AccountDAO Error] Query account failed: " + e.getMessage());
        }
        return null;
    }

    /**
     * Retrieves all accounts registered in the bank.
     * Module 4: Uses Java Collections List & ArrayList.
     */
    public List<Account> getAllAccounts() {
        List<Account> list = new ArrayList<>();
        String sql = "SELECT account_no, holder_name, account_type, balance, extra_param, status, created_at FROM accounts ORDER BY account_no ASC;";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapRowToAccount(rs));
            }
        } catch (SQLException e) {
            System.err.println("[AccountDAO Error] Query all accounts failed: " + e.getMessage());
        }
        return list;
    }

    /**
     * Checks if an account number already exists.
     */
    public boolean accountExists(String accountNo) {
        String sql = "SELECT 1 FROM accounts WHERE account_no = ?;";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, accountNo);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Helper to map a JDBC ResultSet row to concrete subclass instances (Polymorphism).
     */
    private Account mapRowToAccount(ResultSet rs) throws SQLException {
        String accNo = rs.getString("account_no");
        String name = rs.getString("holder_name");
        String type = rs.getString("account_type");
        double balance = rs.getDouble("balance");
        double extraParam = rs.getDouble("extra_param");
        String status = rs.getString("status");
        String createdStr = rs.getString("created_at");

        LocalDateTime createdAt;
        try {
            createdAt = LocalDateTime.parse(createdStr, DT_FORMATTER);
        } catch (Exception e) {
            createdAt = LocalDateTime.now();
        }

        if ("SAVINGS".equalsIgnoreCase(type)) {
            return new SavingsAccount(accNo, name, balance, extraParam, status, createdAt);
        } else {
            return new CurrentAccount(accNo, name, balance, extraParam, status, createdAt);
        }
    }

    /**
     * Generates the next sequential unique Account Number (e.g. SB1002, CA2002).
     */
    public String generateNextAccountNumber(String type) {
        String prefix = "SAVINGS".equalsIgnoreCase(type) ? "SB" : "CA";
        String sql = "SELECT account_no FROM accounts WHERE account_no LIKE ? ORDER BY LENGTH(account_no) DESC, account_no DESC LIMIT 1;";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, prefix + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String lastNo = rs.getString("account_no");
                    int num = Integer.parseInt(lastNo.substring(2));
                    return String.format("%s%04d", prefix, num + 1);
                }
            }
        } catch (Exception e) {
            // fallback
        }
        return prefix + ("SB".equals(prefix) ? "1001" : "2001");
    }
}
