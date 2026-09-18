# Bank Account Management System (BAMS)

**Subject:** Programming in Java
**Student Name:** Vardaan Yadav
**Institution:** VIT, School of Computer Science and Engineering  

---

## Overview

BAMS (Bank Account Management System) is a console-based banking application built entirely in Java. The idea behind this project was to simulate how a real bank system works — from opening accounts and handling deposits/withdrawals to transferring money between accounts and generating proper statements.

The system supports two types of accounts — Savings and Current — each with their own rules (like minimum balance for savings, and overdraft for current accounts). All data is saved to a local SQLite database, so nothing is lost when the app is closed and reopened. It also has basic thread safety built in, so concurrent operations like fund transfers do not cause data corruption.

This project was built as part of the Programming in Java course and covers everything from core OOP concepts to JDBC, File I/O, and Multithreading.

---

## Features

- **Open Savings Account** — Requires a minimum deposit of Rs. 1,000 and supports interest calculation
- **Open Current Account** — Allows overdraft up to a set limit, suitable for business use
- **Check Account Details** — View balance, account status, type, and creation date
- **Deposit Money** — Add funds to any active account with optional remarks
- **Withdraw Money** — Withdraw with automatic validation (min balance / overdraft checks)
- **Transfer Between Accounts** — Atomic fund transfer between two accounts with deadlock-safe locking
- **View Transaction History** — A passbook-style view showing all transactions for an account
- **Export Account Statement** — Saves a formatted `.txt` statement file to disk
- **Close an Account** — Deactivates an account so no further transactions can be made
- **List All Accounts** — Admin view showing every registered account with status and balance

### Application Menu

```
[1]  Open Savings Account        (Rs.1000 min balance, earns interest)
[2]  Open Current Account        (overdraft allowed, business use)
[3]  Check Account Details       (view balance, status, and details)
[4]  Deposit Money               (add funds to an account)
[5]  Withdraw Money              (withdraw funds with balance/overdraft validation)
[6]  Transfer Between Accounts   (atomic fund transfer between two accounts)
[7]  View Transaction History    (passbook view with optional statement export)
[8]  Close an Account            (deactivate an active account)
[9]  List All Accounts           (admin view - see every account)
[0]  Exit
```

---

## Technologies & Tools Used

| Technology / Tool | Purpose |
|---|---|
| **Java (JDK 8+)** | Core programming language |
| **SQLite** | Lightweight local database — no server needed |
| **JDBC** | Java Database Connectivity for running SQL queries |
| **sqlite-jdbc-3.45.1.0.jar** | SQLite driver for Java |
| **SLF4J** | Logging dependency required by the SQLite driver |
| **ConcurrentHashMap** | Thread-safe in-memory cache for fast account lookups |
| **Java File I/O (Streams)** | `FileWriter` -> `BufferedWriter` -> `PrintWriter` for statement export |
| **Java Multithreading** | `synchronized` methods and lock ordering for safe concurrent transfers |

---

## Project Structure

```
BAMS/
├── LAUNCH.bat                            <- Double-click launcher (auto-compiles and runs)
├── README.md                             <- Project documentation
├── statement.md                          <- Academic problem statement and scope
├── src/
│   └── com/bank/
│       ├── main/
│       │   └── BankApp.java              <- Entry point (console UI and menu routing)
│       ├── model/
│       │   ├── Account.java              <- Abstract base class
│       │   ├── SavingsAccount.java       <- Extends Account (min balance & interest)
│       │   ├── CurrentAccount.java       <- Extends Account (overdraft support)
│       │   ├── Transaction.java          <- Represents an immutable transaction record
│       │   └── TransactionType.java      <- Enum (DEPOSIT, WITHDRAWAL, TRANSFER_IN, etc.)
│       ├── exception/
│       │   ├── AccountNotFoundException.java
│       │   ├── InsufficientBalanceException.java
│       │   ├── InvalidAmountException.java
│       │   └── OverdraftLimitExceededException.java
│       ├── service/
│       │   ├── AccountOperations.java    <- Core banking interface
│       │   └── BankService.java          <- Business logic, cache, & concurrency control
│       ├── dao/
│       │   ├── AccountDAO.java           <- JDBC database operations for accounts
│       │   ├── TransactionDAO.java       <- JDBC database operations for transactions
│       │   └── DBConnection.java         <- SQLite connection and schema initialization
│       └── util/
│           ├── ConsoleUtils.java         <- Scanner & input validation helper
│           └── StatementGenerator.java   <- File I/O statement generator (.txt exporter)
├── lib/
│   ├── sqlite-jdbc-3.45.1.0.jar          <- SQLite JDBC Driver
│   ├── slf4j-api-1.7.36.jar              <- Logging API dependency
│   └── slf4j-simple-1.7.36.jar           <- Logger implementation
├── bin/                                  <- Compiled Java bytecode (.class files)
├── data/                                 <- SQLite database file (bank.db)
└── statements/                           <- Exported account statement files (.txt)
```

---

## Steps to Install & Run

### Prerequisites
- **Java JDK 8 or above** must be installed and added to the system PATH
- No other installation or setup is required — SQLite runs as a local file

### Option 1 — Automatic (Recommended)

Just **double-click `LAUNCH.bat`** from the project folder.  
It automatically compiles all source files and launches the application. No manual steps needed.

### Option 2 — Manual via Terminal

Open a terminal (Command Prompt or PowerShell) inside the `BAMS/` folder and run:

**Step 1 – Compile all source files:**
```bash
javac -cp "lib\*" -d bin src\com\bank\model\*.java src\com\bank\exception\*.java src\com\bank\dao\*.java src\com\bank\service\*.java src\com\bank\util\*.java src\com\bank\main\*.java
```

**Step 2 – Run the application:**
```bash
java -cp "bin;lib\*" com.bank.main.BankApp
```

### Account Number Format

| Type | Format | Example | Description |
|------|--------|---------|-------------|
| **Savings** | `SB` + 4 digits | `SB1001`, `SB1002` | Minimum balance Rs. 1,000; earns interest |
| **Current** | `CA` + 4 digits | `CA2001`, `CA2002` | Overdraft facility enabled for businesses |

---

## Instructions for Testing

A demo savings account (`SB1001 — Ankit Pathak, Rs. 15,000`) is automatically seeded into the database on the first run, so the system can be tested right away without needing to create an account first.

Here are some suggested test flows:

1. **Open a new account** — Try menu option `[1]` or `[2]` and enter a name and initial deposit
2. **Deposit money** — Use `[4]`, enter a valid account number and an amount
3. **Withdraw money** — Use `[5]` and try both valid and invalid amounts (e.g., more than the balance) to see exception handling in action
4. **Transfer funds** — Use `[6]` to transfer between two accounts; try transferring to a non-existent account to see `AccountNotFoundException`
5. **View transaction history** — Use `[7]` to see a passbook view, then choose to export it as a `.txt` file saved in the `statements/` folder
6. **Close an account** — Use `[8]` and then try transacting on it to verify it gets rejected
7. **List all accounts** — Use `[9]` for an admin overview of every account in the system

> **Note:** The database persists across runs. To start completely fresh, delete `data/bank.db` — it will be recreated with the demo seed data on the next launch.

---

## Java Concepts Demonstrated

| Concept | Where in Code | Purpose |
|---------|---------------|---------|
| **Abstract Class** | `Account.java` | Base template for all account types |
| **Inheritance** | `SavingsAccount`, `CurrentAccount` | Specialized behavior per account type |
| **Polymorphism** | `withdraw()`, `displayAccountDetails()` | Runtime method dispatch |
| **Interfaces** | `AccountOperations.java` | Clean contract between UI and service layer |
| **Custom Exceptions** | `exception/` package | Meaningful, specific error messages |
| **Exception Handling** | `try-catch-finally`, `try-with-resources` | Safe resource management |
| **Thread Synchronization** | `synchronized` methods + lock ordering | Deadlock-free concurrent transfers |
| **Concurrency Utilities** | `ConcurrentHashMap`, `AtomicInteger` | Thread-safe cache and ID generation |
| **Collections Framework** | `List`, `ArrayList`, `Map` | In-memory data handling |
| **JDBC & Database** | `AccountDAO`, `TransactionDAO`, `DBConnection` | Persistent relational storage via SQLite |
| **File I/O & Streams** | `StatementGenerator.java` | Chained character stream for `.txt` export |
| **Enums** | `TransactionType.java` | Type-safe transaction classification |
| **Input Validation** | `ConsoleUtils.java` | Clean, crash-proof user input handling |
