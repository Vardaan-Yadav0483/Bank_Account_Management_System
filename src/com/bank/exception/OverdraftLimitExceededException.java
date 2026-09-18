package com.bank.exception;

// Thrown when a current account withdrawal exceeds the overdraft credit limit
public class OverdraftLimitExceededException extends InsufficientBalanceException {

    private static final long serialVersionUID = 1L;
    private final double overdraftLimit;

    public OverdraftLimitExceededException(double requestedAmount, double currentBalance, double overdraftLimit) {
        super(
            "You've exceeded your overdraft limit! Max allowed credit: Rs. " +
            String.format("%.2f", overdraftLimit) +
            ". Your current balance is Rs. " + String.format("%.2f", currentBalance) + ".",
            requestedAmount,
            currentBalance
        );
        this.overdraftLimit = overdraftLimit;
    }

    public double getOverdraftLimit() { return overdraftLimit; }
}
