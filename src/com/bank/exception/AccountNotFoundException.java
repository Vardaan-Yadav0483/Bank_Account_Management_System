package com.bank.exception;

// Thrown when an account number doesn't exist in the system
public class AccountNotFoundException extends Exception {

    private static final long serialVersionUID = 1L;
    private final String accountNumber;

    public AccountNotFoundException(String accountNumber) {
        super("Account '" + accountNumber + "' does not exist. Please check the account number and try again.");
        this.accountNumber = accountNumber;
    }

    public AccountNotFoundException(String message, String accountNumber) {
        super(message);
        this.accountNumber = accountNumber;
    }

    public String getAccountNumber() { return accountNumber; }
}
