package com.bank.model;

import com.bank.exception.InsufficientBalanceException;
import com.bank.exception.InvalidAmountException;

import java.time.LocalDateTime;

// Savings Account - extends Account.
// Enforces a minimum balance of Rs.1000 and earns annual interest.
public class SavingsAccount extends Account {

    public static final double MINIMUM_BALANCE = 1000.00;
    private double interestRate;

    // Full constructor - used when loading from database
    public SavingsAccount(String accountNumber, String holderName, double initialBalance,
                          double interestRate, String status, LocalDateTime createdAt) {
        super(accountNumber, holderName, initialBalance, status, createdAt);
        this.interestRate = interestRate > 0 ? interestRate : 4.00;
    }

    // Constructor for creating a new account
    public SavingsAccount(String accountNumber, String holderName, double initialBalance, double interestRate) {
        super(accountNumber, holderName, initialBalance);
        this.interestRate = interestRate > 0 ? interestRate : 4.00;
    }

    // Default constructor - uses 4% interest rate
    public SavingsAccount(String accountNumber, String holderName, double initialBalance) {
        this(accountNumber, holderName, initialBalance, 4.00);
    }

    // Withdrawal is only allowed if balance stays at or above MINIMUM_BALANCE
    @Override
    public synchronized void withdraw(double amount) throws InsufficientBalanceException, InvalidAmountException {
        if (amount <= 0) {
            throw new InvalidAmountException("Withdrawal amount must be more than zero. You entered: " + amount);
        }
        if (!isActive()) {
            throw new IllegalStateException("This account is closed. Cannot withdraw from: " + accountNumber);
        }

        double maxAllowed = this.balance - MINIMUM_BALANCE;
        if (amount > maxAllowed) {
            throw new InsufficientBalanceException(
                "Can't withdraw Rs." + String.format("%.2f", amount) +
                " - your savings account must keep at least Rs." + String.format("%.2f", MINIMUM_BALANCE) + " at all times.",
                amount, this.balance
            );
        }

        this.balance -= amount;
    }

    // Calculates annual simple interest: balance * rate / 100
    @Override
    public double calculateInterestOrCharge() {
        return (this.balance * interestRate) / 100.0;
    }

    @Override
    public void displayAccountDetails() {
        System.out.println("------------------------------------------------------------");
        System.out.println("              SAVINGS ACCOUNT DETAILS                       ");
        System.out.println("------------------------------------------------------------");
        System.out.printf("  Account Number      : %s%n", accountNumber);
        System.out.printf("  Account Holder      : %s%n", holderName);
        System.out.printf("  Account Type        : %s%n", getAccountType());
        System.out.printf("  Current Balance     : Rs. %.2f%n", balance);
        System.out.printf("  Min Balance Required: Rs. %.2f%n", MINIMUM_BALANCE);
        System.out.printf("  Annual Interest Rate: %.2f%%%n", interestRate);
        System.out.printf("  Estimated Interest  : Rs. %.2f per year%n", calculateInterestOrCharge());
        System.out.printf("  Status              : %s%n", status);
        System.out.printf("  Account Opened On   : %s%n", getFormattedCreatedAt());
        System.out.println("------------------------------------------------------------");
    }

    @Override
    public String getAccountType() { return "SAVINGS"; }

    @Override
    public double getExtraParameter() { return interestRate; }

    public double getInterestRate() { return interestRate; }

    public void setInterestRate(double interestRate) { this.interestRate = interestRate; }
}
