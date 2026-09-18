package com.bank.util;

import com.bank.model.Account;
import com.bank.model.Transaction;
import com.bank.model.TransactionType;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/*
 * StatementGenerator.java
 *
 * Unit 4: Java File I/O (Character-Oriented Streams)
 *
 * This class saves a bank statement to a .txt file on the computer.
 * We use Java's file writing classes to do this:
 *
 *   FileWriter    -> opens/creates a file (low-level)
 *   BufferedWriter -> wraps FileWriter to add buffering (improves performance)
 *   PrintWriter   -> wraps BufferedWriter to give us printf/println methods
 *
 * This is a classic "stream chaining" / "wrapper" pattern taught in Unit 4.
 * Stream: FileWriter -> BufferedWriter -> PrintWriter
 *
 * We use try-with-resources (try(...){}) so streams are auto-closed
 * even if an exception occurs - no need to call close() manually.
 * This is the recommended way since Java 7.
 *
 * The output file is saved in a 'statements/' folder in the project directory.
 */
public class StatementGenerator {

    private static final String OUTPUT_FOLDER = "statements"; // folder to save files in
    private static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /*
     * Generates and writes a bank statement to a .txt file.
     *
     * @param account      - the account to generate statement for
     * @param transactions - list of all transactions for this account
     * @return the File object pointing to the saved file
     * @throws IOException if file writing fails
     */
    public static File generateStatementFile(Account account, List<Transaction> transactions) throws IOException {

        // Create the 'statements' folder if it doesn't exist yet
        File dir = new File(OUTPUT_FOLDER);
        if (!dir.exists()) {
            dir.mkdirs(); // mkdirs() creates all parent directories too
        }

        // File name format: statement_SB1001.txt
        String fileName = "statement_" + account.getAccountNumber() + ".txt";
        File outputFile = new File(dir, fileName);

        /*
         * Unit 4: try-with-resources
         * All three streams are opened here and auto-closed when the block ends.
         * This prevents resource leaks (forgetting to close files is a common bug).
         *
         * Stream pipeline:
         *   FileWriter (raw bytes) -> BufferedWriter (buffered, faster) -> PrintWriter (convenient print methods)
         */
        try (FileWriter fw = new FileWriter(outputFile);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter pw = new PrintWriter(bw)) {

            // Write the header section
            pw.println("====================================================================================================");
            pw.println("                                    BANK ACCOUNT STATEMENT                                          ");
            pw.println("                                   Bank Account Management System                                   ");
            pw.println("====================================================================================================");
            pw.printf(" Generated On       : %s%n", LocalDateTime.now().format(DT_FORMAT));
            pw.printf(" Account Number     : %s%n", account.getAccountNumber());
            pw.printf(" Account Holder     : %s%n", account.getHolderName());
            pw.printf(" Account Type       : %s%n", account.getAccountType());
            pw.printf(" Status             : %s%n", account.getStatus());
            pw.printf(" Account Opened On  : %s%n", account.getFormattedCreatedAt());
            pw.println("----------------------------------------------------------------------------------------------------");
            pw.println("TRANSACTION LEDGER:");
            pw.printf("%-20s | %-12s | %-16s | %12s | %14s | %s%n",
                    "Date & Time", "Txn ID", "Type", "Amount (Rs.)", "Balance (Rs.)", "Description");
            pw.println("----------------------------------------------------------------------------------------------------");

            // Track totals for the summary section
            double totalDeposited = 0.0;
            double totalWithdrawn = 0.0;

            if (transactions == null || transactions.isEmpty()) {
                pw.println("                              No transactions found for this account.                              ");
            } else {
                // Unit 4: Loop through the List<Transaction> (ArrayList) and write each row
                for (Transaction tx : transactions) {
                    pw.printf("%-20s | %-12s | %-16s | %12.2f | %14.2f | %s%n",
                            tx.getFormattedTimestamp(),
                            tx.getTransactionId(),
                            tx.getType().name(),
                            tx.getAmount(),
                            tx.getBalanceAfter(),
                            tx.getRemarks());

                    // Accumulate totals
                    if (tx.getType() == TransactionType.DEPOSIT || tx.getType() == TransactionType.TRANSFER_IN) {
                        totalDeposited += tx.getAmount();
                    } else if (tx.getType() == TransactionType.WITHDRAWAL || tx.getType() == TransactionType.TRANSFER_OUT) {
                        totalWithdrawn += tx.getAmount();
                    }
                }
            }

            // Write the summary at the bottom
            pw.println("----------------------------------------------------------------------------------------------------");
            pw.println("ACCOUNT SUMMARY:");
            pw.printf("  Total Money Received (Deposits + Credits)  : Rs. %.2f%n", totalDeposited);
            pw.printf("  Total Money Spent (Withdrawals + Debits)   : Rs. %.2f%n", totalWithdrawn);
            pw.printf("  Closing Balance                            : Rs. %.2f%n", account.getBalance());

            // Show estimated interest for savings accounts
            if ("SAVINGS".equalsIgnoreCase(account.getAccountType())) {
                pw.printf("  Estimated Annual Interest                  : Rs. %.2f%n", account.calculateInterestOrCharge());
            }

            pw.println("====================================================================================================");
            pw.println("                         *** End of Statement - Auto-Generated by BAMS ***                         ");
            pw.println("====================================================================================================");
        }
        // File streams are automatically closed here (try-with-resources handles it)

        return outputFile;
    }
}
