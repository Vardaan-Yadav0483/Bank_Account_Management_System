# Problem Statement – Bank Account Management System (BAMS)

**Course:** Programming in Java (2nd Year B.Tech)  
**Project Type:** Console-based Java Application  

---

## 1. Problem Statement

Managing bank accounts manually is slow and error-prone. This project builds a complete digital banking management system in Java where you can:

- Open and manage Savings and Current accounts
- Deposit and withdraw funds with strict business rules
- Transfer money atomically between accounts
- View transaction history (passbook) and export formatted account statements to disk
- Close / deactivate accounts safely
- List all registered bank accounts (administrative directory view)

The system ensures that data is never lost (persisted via SQLite and JDBC), invalid operations are prevented with meaningful feedback (custom exception hierarchy), concurrent operations maintain data consistency (thread synchronization), and statement reports can be written to disk (Java File I/O streams).

---

## 2. Scope of the Project

This project covers the following banking operations:

1. **Account Management**
   - Open a Savings Account (maintains a minimum balance of Rs. 1,000, accrues interest)
   - Open a Current Account (supports overdraft limit for business transactions)
   - View detailed account status, balances, and metadata
   - Close / deactivate existing accounts
   - List all registered accounts in the system with their status and balances (Admin view)

2. **Transactions & Fund Operations**
   - Deposit money into active accounts with optional transaction remarks
   - Withdraw money with account-specific validations (minimum balance and overdraft limit checks)
   - Transfer funds between two distinct accounts with deadlock-free synchronization and rollback protection

3. **Transaction History & Statement Export**
   - View chronological transaction history (passbook view) with transaction IDs, types, amounts, and post-transaction balances
   - Export official account statements to formatted text files (`statements/statement_<accNo>.txt`) with full transaction ledgers, summaries, and interest calculations

4. **Data Persistence & File I/O**
   - Relational database persistence using SQLite via JDBC (`PreparedStatement`, `ResultSet`, CRUD operations)
   - Zero-server configuration portable database (`data/bank.db`)
   - Character-oriented stream pipeline (`FileWriter` -> `BufferedWriter` -> `PrintWriter`) using try-with-resources for auto-closing files

---

## 3. Who Uses This System?

- **Bank Staff / Administrators**: Open new accounts, perform counter deposits and withdrawals, close accounts, and view all accounts registered in the bank.
- **Customers**: Check balances, review transaction history, and generate offline account statements.
- **Students / Evaluators**: Observe how core Java concepts (OOP, Custom Exceptions, Multithreading & Synchronization, JDBC, Collections, and File I/O) are integrated into a production-grade architecture.

---

## 4. Key Features

- **OOP Design**: Abstract base class `Account` extended by `SavingsAccount` and `CurrentAccount`, leveraging encapsulation, inheritance, and runtime polymorphism.
- **Custom Exceptions**: Clear, informative error handling with dedicated exception classes (`AccountNotFoundException`, `InsufficientBalanceException`, `InvalidAmountException`, and `OverdraftLimitExceededException`).
- **Thread Safety**: Synchronized methods and ordered resource locking to eliminate race conditions and avoid deadlocks during concurrent operations.
- **SQLite Database via JDBC**: Automatic schema initialization, parameterized queries to prevent SQL injection, and durable persistence across application restarts.
- **File I/O Reporting**: Clean stream wrapper chaining to generate formatted customer account statement documents.
- **Robust Console Interface**: Validated input reading routines that prevent application crashes from bad input.
