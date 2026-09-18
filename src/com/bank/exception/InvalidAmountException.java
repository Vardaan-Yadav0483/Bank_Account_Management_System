package com.bank.exception;

// Thrown when someone tries to deposit or withdraw an invalid amount (zero or negative)
public class InvalidAmountException extends Exception {

    private static final long serialVersionUID = 1L;
    private final double amount;

    public InvalidAmountException(double amount) {
        super("Invalid amount: Rs. " + String.format("%.2f", amount) + ". Please enter a value greater than zero.");
        this.amount = amount;
    }

    public InvalidAmountException(String message) {
        super(message);
        this.amount = 0.0;
    }

    public double getAmount() { return amount; }
}
