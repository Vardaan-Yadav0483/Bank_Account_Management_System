package com.bank.service;

import com.bank.exception.AccountNotFoundException;
import com.bank.exception.InsufficientBalanceException;
import com.bank.exception.InvalidAmountException;
import com.bank.model.Account;
import com.bank.model.Transaction;

import java.util.List;

// Interface that defines all banking operations BankService must implement
public interface AccountOperations {

    Account openSavingsAccount(String holderName, double initialDeposit, double interestRate)
            throws InvalidAmountException;

    Account openCurrentAccount(String holderName, double initialDeposit, double overdraftLimit)
            throws InvalidAmountException;

    void deposit(String accountNumber, double amount, String remarks)
            throws AccountNotFoundException, InvalidAmountException;

    void withdraw(String accountNumber, double amount, String remarks)
            throws AccountNotFoundException, InsufficientBalanceException, InvalidAmountException;

    void transfer(String fromAccountNo, String toAccountNo, double amount, String remarks)
            throws AccountNotFoundException, InsufficientBalanceException, InvalidAmountException;

    boolean closeAccount(String accountNumber) throws AccountNotFoundException;

    Account getAccountDetails(String accountNumber) throws AccountNotFoundException;

    List<Transaction> getAccountStatement(String accountNumber) throws AccountNotFoundException;

    List<Account> listAllAccounts();
}
