package com.bank.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Represents a single financial transaction (deposit, withdrawal, transfer).
// Fields are final because transactions should never be modified after recording.
public class Transaction {

    private final String transactionId;
    private final String accountNumber;
    private final TransactionType type;
    private final double amount;
    private final double balanceAfter;
    private final LocalDateTime timestamp;
    private final String remarks;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Full constructor - used when loading transactions from database
    public Transaction(String transactionId, String accountNumber, TransactionType type,
                       double amount, double balanceAfter, LocalDateTime timestamp, String remarks) {
        this.transactionId = transactionId;
        this.accountNumber = accountNumber;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
        this.remarks = remarks != null ? remarks : "-";
    }

    // Constructor for new transactions (auto-sets timestamp to now)
    public Transaction(String transactionId, String accountNumber, TransactionType type,
                       double amount, double balanceAfter, String remarks) {
        this(transactionId, accountNumber, type, amount, balanceAfter, LocalDateTime.now(), remarks);
    }

    public String getTransactionId() { return transactionId; }

    public String getAccountNumber() { return accountNumber; }

    public TransactionType getType() { return type; }

    public double getAmount() { return amount; }

    public double getBalanceAfter() { return balanceAfter; }

    public LocalDateTime getTimestamp() { return timestamp; }

    public String getFormattedTimestamp() { return timestamp.format(FORMATTER); }

    public String getRemarks() { return remarks; }

    @Override
    public String toString() {
        return String.format("[%s] %-15s | %-12s | Amount: %10.2f | Balance: %10.2f | %s",
                getFormattedTimestamp(), transactionId, type.name(), amount, balanceAfter, remarks);
    }
}
