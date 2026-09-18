package com.bank.service;

import com.bank.dao.AccountDAO;
import com.bank.dao.TransactionDAO;
import com.bank.exception.AccountNotFoundException;
import com.bank.exception.InsufficientBalanceException;
import com.bank.exception.InvalidAmountException;
import com.bank.model.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// Handles all the business logic for the banking system.
// Sits between the UI (BankApp) and the database (DAO classes).
public class BankService implements AccountOperations {

    private final AccountDAO accountDAO;
    private final TransactionDAO transactionDAO;

    // In-memory cache for fast account lookups (avoids hitting the DB every time)
    private final Map<String, Account> accountCache;

    public BankService() {
        this.accountDAO = new AccountDAO();
        this.transactionDAO = new TransactionDAO();
        this.accountCache = new ConcurrentHashMap<>();
        loadAccountsIntoCache();
    }

    // Loads all accounts from the database into memory on startup
    private void loadAccountsIntoCache() {
        List<Account> accounts = accountDAO.getAllAccounts();
        for (Account acc : accounts) {
            accountCache.put(acc.getAccountNumber(), acc);
        }
    }

    // Opens a new savings account and saves it to the database
    @Override
    public synchronized Account openSavingsAccount(String holderName, double initialDeposit, double interestRate)
            throws InvalidAmountException {

        if (initialDeposit < SavingsAccount.MINIMUM_BALANCE) {
            throw new InvalidAmountException(
                "Opening deposit Rs." + String.format("%.2f", initialDeposit) +
                " is less than the required minimum of Rs." + String.format("%.2f", SavingsAccount.MINIMUM_BALANCE)
            );
        }

        String accNo = accountDAO.generateNextAccountNumber("SAVINGS");
        SavingsAccount acc = new SavingsAccount(accNo, holderName, initialDeposit, interestRate);

        boolean saved = accountDAO.saveAccount(acc);
        if (saved) {
            accountCache.put(accNo, acc);
            String txId = transactionDAO.generateNextTransactionId();
            Transaction tx = new Transaction(txId, accNo, TransactionType.DEPOSIT, initialDeposit,
                                             initialDeposit, "Account Opening Deposit");
            transactionDAO.recordTransaction(tx);
            return acc;
        } else {
            throw new RuntimeException("Failed to save account to the database.");
        }
    }

    // Opens a new current account with an overdraft limit
    @Override
    public synchronized Account openCurrentAccount(String holderName, double initialDeposit, double overdraftLimit)
            throws InvalidAmountException {

        if (initialDeposit < 0) {
            throw new InvalidAmountException("Opening deposit cannot be negative.");
        }

        String accNo = accountDAO.generateNextAccountNumber("CURRENT");
        CurrentAccount acc = new CurrentAccount(accNo, holderName, initialDeposit, overdraftLimit);

        boolean saved = accountDAO.saveAccount(acc);
        if (saved) {
            accountCache.put(accNo, acc);
            if (initialDeposit > 0) {
                String txId = transactionDAO.generateNextTransactionId();
                Transaction tx = new Transaction(txId, accNo, TransactionType.DEPOSIT, initialDeposit,
                                                 initialDeposit, "Account Opening Deposit");
                transactionDAO.recordTransaction(tx);
            }
            return acc;
        } else {
            throw new RuntimeException("Failed to save account to the database.");
        }
    }

    // Deposits money into an account (synchronized on the account object for thread safety)
    @Override
    public void deposit(String accountNumber, double amount, String remarks)
            throws AccountNotFoundException, InvalidAmountException {

        Account account = getAccountDetails(accountNumber);
        synchronized (account) {
            account.deposit(amount);
            accountDAO.updateBalance(accountNumber, account.getBalance());

            String txId = transactionDAO.generateNextTransactionId();
            Transaction tx = new Transaction(txId, accountNumber, TransactionType.DEPOSIT, amount,
                                             account.getBalance(), remarks != null ? remarks : "Cash Deposit");
            transactionDAO.recordTransaction(tx);
        }
    }

    // Withdraws money from an account
    @Override
    public void withdraw(String accountNumber, double amount, String remarks)
            throws AccountNotFoundException, InsufficientBalanceException, InvalidAmountException {

        Account account = getAccountDetails(accountNumber);
        synchronized (account) {
            account.withdraw(amount);
            accountDAO.updateBalance(accountNumber, account.getBalance());

            String txId = transactionDAO.generateNextTransactionId();
            Transaction tx = new Transaction(txId, accountNumber, TransactionType.WITHDRAWAL, amount,
                                             account.getBalance(), remarks != null ? remarks : "ATM Withdrawal");
            transactionDAO.recordTransaction(tx);
        }
    }

    // Transfers money between two accounts.
    // Uses lock ordering to prevent deadlock when two transfers happen simultaneously.
    @Override
    public void transfer(String fromAccountNo, String toAccountNo, double amount, String remarks)
            throws AccountNotFoundException, InsufficientBalanceException, InvalidAmountException {

        if (fromAccountNo.equalsIgnoreCase(toAccountNo)) {
            throw new IllegalArgumentException("You can't transfer money to the same account!");
        }

        Account fromAccount = getAccountDetails(fromAccountNo);
        Account toAccount = getAccountDetails(toAccountNo);

        if (!toAccount.isActive()) {
            throw new IllegalStateException("Destination account " + toAccountNo + " is closed or inactive.");
        }

        // Lock accounts in a consistent order to avoid deadlock
        Account firstLock  = fromAccountNo.compareTo(toAccountNo) < 0 ? fromAccount : toAccount;
        Account secondLock = fromAccountNo.compareTo(toAccountNo) < 0 ? toAccount : fromAccount;

        synchronized (firstLock) {
            synchronized (secondLock) {
                fromAccount.withdraw(amount);
                try {
                    toAccount.deposit(amount);
                } catch (Exception e) {
                    try {
                        fromAccount.deposit(amount);
                    } catch (Exception ignored) {}
                    throw e;
                }

                accountDAO.updateBalance(fromAccountNo, fromAccount.getBalance());
                accountDAO.updateBalance(toAccountNo, toAccount.getBalance());

                String txIdDebit = transactionDAO.generateNextTransactionId();
                transactionDAO.recordTransaction(new Transaction(txIdDebit, fromAccountNo,
                        TransactionType.TRANSFER_OUT, amount, fromAccount.getBalance(),
                        "Transfer to " + toAccountNo + " (" + (remarks != null ? remarks : "Online Transfer") + ")"));

                String txIdCredit = transactionDAO.generateNextTransactionId();
                transactionDAO.recordTransaction(new Transaction(txIdCredit, toAccountNo,
                        TransactionType.TRANSFER_IN, amount, toAccount.getBalance(),
                        "Transfer from " + fromAccountNo + " (" + (remarks != null ? remarks : "Online Transfer") + ")"));
            }
        }
    }

    // Closes an account by setting its status to CLOSED
    @Override
    public synchronized boolean closeAccount(String accountNumber) throws AccountNotFoundException {
        Account account = getAccountDetails(accountNumber);
        if ("CLOSED".equalsIgnoreCase(account.getStatus())) {
            return false;
        }
        boolean updated = accountDAO.updateStatus(accountNumber, "CLOSED");
        if (updated) {
            account.setStatus("CLOSED");
            String txId = transactionDAO.generateNextTransactionId();
            transactionDAO.recordTransaction(new Transaction(txId, accountNumber,
                    TransactionType.WITHDRAWAL, 0.0, account.getBalance(), "Account Officially Closed"));
        }
        return updated;
    }

    // Returns account details - checks cache first, then database
    @Override
    public Account getAccountDetails(String accountNumber) throws AccountNotFoundException {
        Account account = accountCache.get(accountNumber);
        if (account == null) {
            account = accountDAO.getAccountByNo(accountNumber);
            if (account != null) {
                accountCache.put(accountNumber, account);
            }
        }
        if (account == null) {
            throw new AccountNotFoundException(accountNumber);
        }
        return account;
    }

    // Returns all transactions for a given account
    @Override
    public List<Transaction> getAccountStatement(String accountNumber) throws AccountNotFoundException {
        getAccountDetails(accountNumber);
        return transactionDAO.getTransactionsByAccount(accountNumber);
    }

    // Returns all accounts in the system
    @Override
    public List<Account> listAllAccounts() {
        return accountDAO.getAllAccounts();
    }
}
