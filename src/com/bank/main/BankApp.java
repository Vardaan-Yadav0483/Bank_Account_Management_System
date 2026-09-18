package com.bank.main;

import com.bank.exception.AccountNotFoundException;
import com.bank.exception.InsufficientBalanceException;
import com.bank.exception.InvalidAmountException;
import com.bank.model.Account;
import com.bank.model.SavingsAccount;
import com.bank.model.Transaction;
import com.bank.service.BankService;
import com.bank.util.ConsoleUtils;

import java.util.List;

public class BankApp {

    private static final BankService bankService = new BankService();

    public static void main(String[] args) {
        showWelcomeBanner();

        boolean keepRunning = true;

        while (keepRunning) {
            showMainMenu();
            int choice = ConsoleUtils.readInt("Enter your choice [0-9]: ", 0, 9);

            switch (choice) {
                case 1:
                    openSavingsAccount();
                    break;
                case 2:
                    openCurrentAccount();
                    break;
                case 3:
                    viewAccountDetails();
                    break;
                case 4:
                    depositMoney();
                    break;
                case 5:
                    withdrawMoney();
                    break;
                case 6:
                    transferFunds();
                    break;
                case 7:
                    viewPassbook();
                    break;
                case 8:
                    closeAccount();
                    break;
                case 9:
                    listAllAccounts();
                    break;
                case 0:
                    keepRunning = false;
                    System.out.println("\nThanks for using BAMS! Goodbye :)");
                    break;
                default:
                    System.out.println("Hmm, that's not a valid option. Please try again.");
            }

            if (keepRunning) {
                ConsoleUtils.pause();
            }
        }
    }

    // Prints the welcome banner on startup
    private static void showWelcomeBanner() {
        System.out.println("================================================================================");
        System.out.println("                BANK ACCOUNT MANAGEMENT SYSTEM                         ");
        System.out.println("          Java Project | 2nd Year | Programming in Java                       ");
        System.out.println("================================================================================");
    }

    // Shows the main menu
    private static void showMainMenu() {
        ConsoleUtils.printHeader("MAIN MENU");
        System.out.println("  [1]  Open Savings Account        (minimum balance Rs.1000, earns interest)");
        System.out.println("  [2]  Open Current Account        (overdraft allowed, for business use)");
        System.out.println("  [3]  Check Account Details       (view balance, status, account info)");
        System.out.println("  [4]  Deposit Money               (add money to an account)");
        System.out.println("  [5]  Withdraw Money              (take out money from an account)");
        System.out.println("  [6]  Transfer Between Accounts   (send money from one account to another)");
        System.out.println("  [7]  View Transaction History    (see all past transactions)");
        System.out.println("  [8]  Close an Account           (deactivate/close an account)");
        System.out.println("  [9]  List All Accounts          (admin view - see every account)");
        System.out.println("  [0]  Exit");
        ConsoleUtils.printDivider();
    }

    // Opens a new savings account
    private static void openSavingsAccount() {
        ConsoleUtils.printHeader("OPEN SAVINGS ACCOUNT");
        String name = ConsoleUtils.readString("Enter your full name: ");
        double deposit = ConsoleUtils.readDouble(
                "Enter opening deposit (min Rs. " + SavingsAccount.MINIMUM_BALANCE + "): Rs. ",
                SavingsAccount.MINIMUM_BALANCE
        );
        double rate = ConsoleUtils.readDouble("Enter annual interest rate (%): ", 0.0);

        try {
            Account acc = bankService.openSavingsAccount(name, deposit, rate);
            System.out.println("\n[SUCCESS] Savings account created successfully!");
            acc.displayAccountDetails();
        } catch (InvalidAmountException e) {
            System.err.println("[ERROR] " + e.getMessage());
        } catch (Exception e) {
            System.err.println("[ERROR] Something went wrong: " + e.getMessage());
        }
    }

    // Opens a new current account with overdraft facility
    private static void openCurrentAccount() {
        ConsoleUtils.printHeader("OPEN CURRENT ACCOUNT");
        String name = ConsoleUtils.readString("Enter account holder / business name: ");
        double deposit = ConsoleUtils.readDouble("Enter opening deposit: Rs. ", 0.0);
        double overdraft = ConsoleUtils.readDouble("Enter overdraft limit: Rs. ", 0.0);

        try {
            Account acc = bankService.openCurrentAccount(name, deposit, overdraft);
            System.out.println("\n[SUCCESS] Current account created successfully!");
            acc.displayAccountDetails();
        } catch (InvalidAmountException e) {
            System.err.println("[ERROR] " + e.getMessage());
        } catch (Exception e) {
            System.err.println("[ERROR] Something went wrong: " + e.getMessage());
        }
    }

    // Shows full details of an account
    private static void viewAccountDetails() {
        ConsoleUtils.printHeader("ACCOUNT DETAILS");
        String accNo = ConsoleUtils.readString("Enter account number (e.g. SB1001 or CA2001): ").toUpperCase();

        try {
            Account acc = bankService.getAccountDetails(accNo);
            acc.displayAccountDetails();
        } catch (AccountNotFoundException e) {
            System.err.println("[ERROR] " + e.getMessage());
        }
    }

    // Deposits money into an account
    private static void depositMoney() {
        ConsoleUtils.printHeader("DEPOSIT MONEY");
        String accNo = ConsoleUtils.readString("Enter account number: ").toUpperCase();
        double amount = ConsoleUtils.readDouble("Enter deposit amount: Rs. ", 1.0);
        String note = ConsoleUtils.readStringOptional("Add a note (or press Enter to skip): ", "Cash Deposit");

        try {
            bankService.deposit(accNo, amount, note);
            Account acc = bankService.getAccountDetails(accNo);
            System.out.printf("\n[SUCCESS] Rs. %.2f deposited into account %s.%n", amount, accNo);
            System.out.printf("  Your new balance: Rs. %.2f%n", acc.getBalance());
        } catch (AccountNotFoundException | InvalidAmountException e) {
            System.err.println("[ERROR] Deposit failed: " + e.getMessage());
        }
    }

    // Withdraws money from an account
    private static void withdrawMoney() {
        ConsoleUtils.printHeader("WITHDRAW MONEY");
        String accNo = ConsoleUtils.readString("Enter account number: ").toUpperCase();
        double amount = ConsoleUtils.readDouble("Enter withdrawal amount: Rs. ", 1.0);
        String note = ConsoleUtils.readStringOptional("Add a note (or press Enter to skip): ", "ATM Withdrawal");

        try {
            bankService.withdraw(accNo, amount, note);
            Account acc = bankService.getAccountDetails(accNo);
            System.out.printf("\n[SUCCESS] Rs. %.2f withdrawn from account %s.%n", amount, accNo);
            System.out.printf("  Remaining balance: Rs. %.2f%n", acc.getBalance());
        } catch (AccountNotFoundException | InsufficientBalanceException | InvalidAmountException e) {
            System.err.println("[ERROR] Withdrawal failed: " + e.getMessage());
        }
    }

    // Transfers money from one account to another
    private static void transferFunds() {
        ConsoleUtils.printHeader("TRANSFER MONEY");
        String fromAcc = ConsoleUtils.readString("Enter sender's account number: ").toUpperCase();
        String toAcc = ConsoleUtils.readString("Enter receiver's account number: ").toUpperCase();
        double amount = ConsoleUtils.readDouble("Enter amount to transfer: Rs. ", 1.0);
        String note = ConsoleUtils.readStringOptional("Add a transfer note (or press Enter to skip): ", "Online Transfer");

        try {
            bankService.transfer(fromAcc, toAcc, amount, note);
            Account sender = bankService.getAccountDetails(fromAcc);
            Account receiver = bankService.getAccountDetails(toAcc);

            System.out.println("\n[SUCCESS] Transfer completed!");
            System.out.printf("  Amount transferred : Rs. %.2f%n", amount);
            System.out.printf("  %s balance now     : Rs. %.2f%n", fromAcc, sender.getBalance());
            System.out.printf("  %s balance now     : Rs. %.2f%n", toAcc, receiver.getBalance());
        } catch (AccountNotFoundException | InsufficientBalanceException | InvalidAmountException e) {
            System.err.println("[ERROR] Transfer failed: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("[ERROR] Transfer cancelled: " + e.getMessage());
        }
    }

    // Shows all past transactions for an account (passbook)
    private static void viewPassbook() {
        ConsoleUtils.printHeader("TRANSACTION HISTORY (PASSBOOK)");
        String accNo = ConsoleUtils.readString("Enter account number: ").toUpperCase();

        try {
            Account acc = bankService.getAccountDetails(accNo);
            List<Transaction> transactions = bankService.getAccountStatement(accNo);

            System.out.printf("Account: %s | Holder: %s | Balance: Rs. %.2f%n",
                    acc.getAccountNumber(), acc.getHolderName(), acc.getBalance());
            ConsoleUtils.printDivider();
            System.out.printf("%-20s | %-12s | %-14s | %10s | %10s | %s%n",
                    "Date & Time", "Txn ID", "Type", "Amount", "Balance", "Note");
            ConsoleUtils.printDivider();

            if (transactions.isEmpty()) {
                System.out.println("No transactions yet.");
            } else {
                for (Transaction tx : transactions) {
                    System.out.printf("%-20s | %-12s | %-14s | %10.2f | %10.2f | %s%n",
                            tx.getFormattedTimestamp(),
                            tx.getTransactionId(),
                            tx.getType().name(),
                            tx.getAmount(),
                            tx.getBalanceAfter(),
                            tx.getRemarks());
                }
            }
            ConsoleUtils.printDivider();
        } catch (AccountNotFoundException e) {
            System.err.println("[ERROR] " + e.getMessage());
        }
    }

    // Closes/deactivates an account after user confirmation
    private static void closeAccount() {
        ConsoleUtils.printHeader("CLOSE ACCOUNT");
        String accNo = ConsoleUtils.readString("Enter account number to close: ").toUpperCase();
        String confirm = ConsoleUtils.readString("Are you sure you want to close account " + accNo + "? Type YES to confirm: ");

        if ("Y".equalsIgnoreCase(confirm) || "YES".equalsIgnoreCase(confirm)) {
            try {
                boolean wasClosed = bankService.closeAccount(accNo);
                if (wasClosed) {
                    System.out.println("\n[SUCCESS] Account " + accNo + " has been closed.");
                } else {
                    System.out.println("This account was already closed.");
                }
            } catch (AccountNotFoundException e) {
                System.err.println("[ERROR] " + e.getMessage());
            }
        } else {
            System.out.println("Okay, account closure cancelled.");
        }
    }

    // Lists all accounts in the system (admin view)
    private static void listAllAccounts() {
        ConsoleUtils.printHeader("ALL ACCOUNTS");
        List<Account> accounts = bankService.listAllAccounts();

        System.out.printf("%-10s | %-24s | %-10s | %12s | %-8s | %s%n",
                "Acc No", "Holder Name", "Type", "Balance (Rs.)", "Status", "Opened On");
        ConsoleUtils.printDivider();

        for (Account acc : accounts) {
            System.out.printf("%-10s | %-24s | %-10s | %12.2f | %-8s | %s%n",
                    acc.getAccountNumber(),
                    acc.getHolderName(),
                    acc.getAccountType(),
                    acc.getBalance(),
                    acc.getStatus(),
                    acc.getFormattedCreatedAt());
        }
        ConsoleUtils.printDivider();
        System.out.printf("Total accounts: %d%n", accounts.size());
    }
}
