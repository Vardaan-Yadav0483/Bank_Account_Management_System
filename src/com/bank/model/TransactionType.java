package com.bank.model;

// Enum representing the four types of transactions in the system
public enum TransactionType {
    DEPOSIT("Deposit"),
    WITHDRAWAL("Withdrawal"),
    TRANSFER_IN("Transfer In (Received)"),
    TRANSFER_OUT("Transfer Out (Sent)");

    private final String description;

    TransactionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
