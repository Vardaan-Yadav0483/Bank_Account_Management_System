package com.bank.model;

import com.bank.exception.InvalidAmountException;
import com.bank.exception.OverdraftLimitExceededException;

import java.time.LocalDateTime;

// Current Account - extends Account.
// Allows balance to go negative up to the overdraft limit.
// Useful for business accounts that need credit flexibility.
public class CurrentAccount extends Account {

    private double overdraftLimit;
    public static final double OVERDRAFT_FEE_RATE = 0.02; // 2% service charge on overdrawn amount

    // Full constructor - used when loading from database
    public CurrentAccount(String accountNumber, String holderName, double initialBalance,
                          double overdraftLimit, String status, LocalDateTime createdAt) {
        super(accountNumber, holderName, initialBalance, status, createdAt);
        this.overdraftLimit = overdraftLimit >= 0 ? overdraftLimit : 10000.00;
    }

    // Constructor for creating a new account
    public CurrentAccount(String accountNumber, String holderName, double initialBalance, double overdraftLimit) {
        super(accountNumber, holderName, initialBalance);
        this.overdraftLimit = overdraftLimit >= 0 ? overdraftLimit : 10000.00;
    }

    // Default constructor - uses Rs.10,000 overdraft limit
    public CurrentAccount(String accountNumber, String holderName, double initialBalance) {
        this(accountNumber, holderName, initialBalance, 10000.00);
    }

    // Allows withdrawal up to (balance + overdraftLimit)
    @Override
    public synchronized void withdraw(double amount) throws OverdraftLimitExceededException, InvalidAmountException {
        if (amount <= 0) {
            throw new InvalidAmountException("Withdrawal amount must be more than zero. You entered: " + amount);
        }
        if (!isActive()) {
            throw new IllegalStateException("This account is closed. Cannot withdraw from: " + accountNumber);
        }

        double totalAvailable = this.balance + this.overdraftLimit;
        if (amount > totalAvailable) {
            throw new OverdraftLimitExceededException(amount, this.balance, this.overdraftLimit);
        }

        this.balance -= amount;
    }

    // Returns 2% of the overdrawn amount as a service fee (only when balance is negative)
    @Override
    public double calculateInterestOrCharge() {
        if (this.balance < 0) {
            return Math.abs(this.balance) * OVERDRAFT_FEE_RATE;
        }
        return 0.00;
    }

    @Override
    public void displayAccountDetails() {
        System.out.println("------------------------------------------------------------");
        System.out.println("              CURRENT ACCOUNT DETAILS                       ");
        System.out.println("------------------------------------------------------------");
        System.out.printf("  Account Number     : %s%n", accountNumber);
        System.out.printf("  Account Holder     : %s%n", holderName);
        System.out.printf("  Account Type       : %s%n", getAccountType());
        System.out.printf("  Current Balance    : Rs. %.2f%n", balance);
        System.out.printf("  Overdraft Limit    : Rs. %.2f%n", overdraftLimit);
        System.out.printf("  Total Available    : Rs. %.2f%n", Math.max(0, balance + overdraftLimit));
        if (balance < 0) {
            System.out.printf("  Overdraft Fee Due  : Rs. %.2f%n", calculateInterestOrCharge());
        }
        System.out.printf("  Status             : %s%n", status);
        System.out.printf("  Account Opened On  : %s%n", getFormattedCreatedAt());
        System.out.println("------------------------------------------------------------");
    }

    @Override
    public String getAccountType() { return "CURRENT"; }

    @Override
    public double getExtraParameter() { return overdraftLimit; }

    public double getOverdraftLimit() { return overdraftLimit; }

    public void setOverdraftLimit(double overdraftLimit) { this.overdraftLimit = overdraftLimit; }
}
