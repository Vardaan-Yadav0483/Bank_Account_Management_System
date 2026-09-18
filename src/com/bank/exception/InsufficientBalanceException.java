package com.bank.exception;

// Thrown when a withdrawal fails because the account doesn't have enough funds
public class InsufficientBalanceException extends Exception {

    private static final long serialVersionUID = 1L;
    private final double requestedAmount;
    private final double currentBalance;

    public InsufficientBalanceException(String message) {
        super(message);
        this.requestedAmount = 0.0;
        this.currentBalance = 0.0;
    }

    public InsufficientBalanceException(String message, double requestedAmount, double currentBalance) {
        super(message);
        this.requestedAmount = requestedAmount;
        this.currentBalance = currentBalance;
    }

    public double getRequestedAmount() { return requestedAmount; }

    public double getCurrentBalance() { return currentBalance; }

    @Override
    public String toString() {
        if (requestedAmount > 0) {
            return "InsufficientBalanceException: " + getMessage() +
                   " [Tried to withdraw: Rs. " + String.format("%.2f", requestedAmount) +
                   ", Available: Rs. " + String.format("%.2f", currentBalance) + "]";
        }
        return "InsufficientBalanceException: " + getMessage();
    }
}
