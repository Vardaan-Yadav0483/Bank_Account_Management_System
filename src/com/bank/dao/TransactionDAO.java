package com.bank.dao;

import com.bank.model.Transaction;
import com.bank.model.TransactionType;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Course: CSE2006 - Programming in Java
 * Module 5: Database Applications with JDBC
 * Module 4: Collections Framework (ArrayList, List)
 * 
 * Data Access Object (DAO) for persisting and querying the transaction ledger.
 */
public class TransactionDAO {
    private static final DateTimeFormatter DT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final AtomicInteger lastTxnNumber = new AtomicInteger(100000);
    private boolean counterInitialized = false;

    /**
     * Records a new transaction into the immutable audit ledger.
     */
    public boolean recordTransaction(Transaction tx) {
        String sql = "INSERT INTO transactions (trans_id, account_no, trans_type, amount, balance_after, timestamp, remarks) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?);";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, tx.getTransactionId());
            pstmt.setString(2, tx.getAccountNumber());
            pstmt.setString(3, tx.getType().name());
            pstmt.setDouble(4, tx.getAmount());
            pstmt.setDouble(5, tx.getBalanceAfter());
            pstmt.setString(6, tx.getFormattedTimestamp());
            pstmt.setString(7, tx.getRemarks());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[TransactionDAO Error] Failed to log transaction: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves all transactions associated with a specific account number, ordered chronologically.
     */
    public List<Transaction> getTransactionsByAccount(String accountNo) {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT trans_id, account_no, trans_type, amount, balance_after, timestamp, remarks " +
                     "FROM transactions WHERE account_no = ? ORDER BY timestamp ASC;";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, accountNo);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToTransaction(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[TransactionDAO Error] Query account transactions failed: " + e.getMessage());
        }
        return list;
    }

    /**
     * Retrieves all transactions in the entire banking system.
     */
    public List<Transaction> getAllTransactions() {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT trans_id, account_no, trans_type, amount, balance_after, timestamp, remarks FROM transactions ORDER BY timestamp DESC;";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapRowToTransaction(rs));
            }
        } catch (SQLException e) {
            System.err.println("[TransactionDAO Error] Query all transactions failed: " + e.getMessage());
        }
        return list;
    }

    private synchronized void initCounter() {
        if (counterInitialized) return;
        String sql = "SELECT trans_id FROM transactions ORDER BY LENGTH(trans_id) DESC, trans_id DESC LIMIT 1;";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                String lastId = rs.getString("trans_id");
                if (lastId != null && lastId.startsWith("TXN")) {
                    int num = Integer.parseInt(lastId.substring(3));
                    lastTxnNumber.set(Math.max(lastTxnNumber.get(), num));
                }
            }
        } catch (Exception ignored) {}
        counterInitialized = true;
    }

    /**
     * Generates a unique transaction identifier (e.g. TXN100003).
     * Thread-safe using AtomicInteger backed by persistent database state.
     */
    public String generateNextTransactionId() {
        if (!counterInitialized) {
            initCounter();
        }
        return String.format("TXN%06d", lastTxnNumber.incrementAndGet());
    }

    private Transaction mapRowToTransaction(ResultSet rs) throws SQLException {
        String id = rs.getString("trans_id");
        String accNo = rs.getString("account_no");
        TransactionType type = TransactionType.valueOf(rs.getString("trans_type"));
        double amount = rs.getDouble("amount");
        double balanceAfter = rs.getDouble("balance_after");
        String tsStr = rs.getString("timestamp");
        String remarks = rs.getString("remarks");

        LocalDateTime ts;
        try {
            ts = LocalDateTime.parse(tsStr, DT_FORMATTER);
        } catch (Exception e) {
            ts = LocalDateTime.now();
        }

        return new Transaction(id, accNo, type, amount, balanceAfter, ts, remarks);
    }
}
