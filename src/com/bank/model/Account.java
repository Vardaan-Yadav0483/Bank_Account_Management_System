package com.bank.model;

import com.bank.exception.InsufficientBalanceException;
import com.bank.exception.InvalidAmountException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Abstract base class for all bank accounts.
// SavingsAccount and CurrentAccount both extend this class.
public abstract class Account {

    protected String accountNumber;
    protected String holderName;
    protected double balance;
    protected String status; // "ACTIVE" or "CLOSED"
    protected LocalDateTime createdAt;

    protected static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Full constructor - used when loading from database
    public Account(String accountNumber, String holderName, double initialBalance, String status, LocalDateTime createdAt) {
        this.accountNumber = accountNumber;
        this.holderName = holderName;
        this.balance = initialBalance;
        this.status = (status != null) ? status : "ACTIVE";
        this.createdAt = (createdAt != null) ? createdAt : LocalDateTime.now();
    }

    // Constructor for new accounts (sets status ACTIVE and timestamp to now)
    public Account(String accountNumber, String holderName, double initialBalance) {
        this(accountNumber, holderName, initialBalance, "ACTIVE", LocalDateTime.now());
    }

    // Deposits money into this account (synchronized for thread safety)
    public synchronized void deposit(double amount) throws InvalidAmountException {
        if (amount <= 0) {
            throw new InvalidAmountException("Deposit amount must be positive. You entered: " + amount);
        }
        if (!"ACTIVE".equalsIgnoreCase(this.status)) {
            throw new IllegalStateException("Can't deposit into a closed account: " + accountNumber);
        }
        this.balance += amount;
    }

    // Overloaded deposit that accepts an optional remark
    public synchronized void deposit(double amount, String remarks) throws InvalidAmountException {
        deposit(amount);
    }

    // Abstract - each account type has its own withdrawal rules
    public abstract void withdraw(double amount) throws InsufficientBalanceException, InvalidAmountException;

    // Abstract - savings earns interest, current charges overdraft fee
    public abstract double calculateInterestOrCharge();

    // Abstract - prints account info to the console
    public abstract void displayAccountDetails();

    // Returns "SAVINGS" or "CURRENT"
    public abstract String getAccountType();

    // Returns interest rate (savings) or overdraft limit (current)
    public abstract double getExtraParameter();

    // Getters and setters
    public String getAccountNumber() { return accountNumber; }

    public String getHolderName() { return holderName; }

    public void setHolderName(String holderName) { this.holderName = holderName; }

    public synchronized double getBalance() { return balance; }

    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public String getFormattedCreatedAt() { return createdAt.format(DATE_FORMATTER); }

    public boolean isActive() { return "ACTIVE".equalsIgnoreCase(this.status); }
}
