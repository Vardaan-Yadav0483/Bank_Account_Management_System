package com.bank.util;

import java.util.Scanner;

// Utility class for reading user input from the terminal and printing formatted output.
// All methods are static so they can be called without creating an object.
public class ConsoleUtils {

    private static final Scanner SCANNER = new Scanner(System.in);

    // Prints a centered section header
    public static void printHeader(String title) {
        System.out.println();
        System.out.println("================================================================================");
        int padding = Math.max(0, (80 - title.length()) / 2);
        System.out.printf("%" + padding + "s%s%n", "", title);
        System.out.println("================================================================================");
    }

    // Prints a dashed separator line
    public static void printDivider() {
        System.out.println("--------------------------------------------------------------------------------");
    }

    // Reads a non-empty string from the user, re-prompts if blank
    public static String readString(String prompt) {
        System.out.print(prompt);
        String line = SCANNER.nextLine().trim();
        while (line.isEmpty()) {
            System.out.print("Input can't be blank! Please type something: ");
            line = SCANNER.nextLine().trim();
        }
        return line;
    }

    // Reads a string; returns defaultValue if the user just presses Enter
    public static String readStringOptional(String prompt, String defaultValue) {
        System.out.print(prompt);
        String line = SCANNER.nextLine().trim();
        return line.isEmpty() ? defaultValue : line;
    }

    // Reads a decimal number; keeps asking until value is >= minVal
    public static double readDouble(String prompt, double minVal) {
        while (true) {
            System.out.print(prompt);
            String input = SCANNER.nextLine().trim();
            try {
                double val = Double.parseDouble(input);
                if (val < minVal) {
                    System.out.printf("Amount must be at least Rs. %.2f. Try again: ", minVal);
                    continue;
                }
                return val;
            } catch (NumberFormatException e) {
                System.out.print("That doesn't look like a number. Please enter a valid amount: ");
            }
        }
    }

    // Reads an integer within [min, max]; keeps asking until valid
    public static int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String input = SCANNER.nextLine().trim();
            try {
                int val = Integer.parseInt(input);
                if (val < min || val > max) {
                    System.out.printf("Please enter a number between %d and %d: ", min, max);
                    continue;
                }
                return val;
            } catch (NumberFormatException e) {
                System.out.print("Invalid input! Please enter a whole number: ");
            }
        }
    }

    // Waits for the user to press Enter before returning to the menu
    public static void pause() {
        System.out.print("\nPress Enter to go back to the main menu...");
        SCANNER.nextLine();
    }
}
