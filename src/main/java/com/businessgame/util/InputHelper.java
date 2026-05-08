package com.businessgame.util;

import java.util.Scanner;

/**
 * Thin wrapper around {@link Scanner} that provides safe, descriptive
 * prompts and centralised error handling for console input.
 *
 * <p>The original code called {@code sc.nextInt()} throughout without any
 * validation.  If the user typed a non-integer the scanner would throw
 * {@link java.util.InputMismatchException} and the game would crash.
 * This helper retries until valid input is received.</p>
 */
public final class InputHelper {

    private final Scanner sc;

    public InputHelper(Scanner sc) {
        this.sc = sc;
    }

    /**
     * Reads the next integer from standard input, retrying on bad input.
     *
     * @param prompt message shown to the user before reading
     * @return the integer entered by the user
     */
    public int readInt(String prompt) {
        while (true) {
            System.out.print(prompt + ": ");
            if (sc.hasNextInt()) {
                int value = sc.nextInt();
                sc.nextLine(); // consume trailing newline
                return value;
            }
            System.out.println("  Invalid input — please enter a whole number.");
            sc.nextLine(); // discard bad token
        }
    }

    /**
     * Reads the next long integer, retrying on bad input.
     */
    public long readLong(String prompt) {
        while (true) {
            System.out.print(prompt + ": ");
            if (sc.hasNextLong()) {
                long value = sc.nextLong();
                sc.nextLine();
                return value;
            }
            System.out.println("  Invalid input — please enter a whole number.");
            sc.nextLine();
        }
    }

    /**
     * Reads the next non-blank word, retrying on blank input.
     */
    public String readWord(String prompt) {
        while (true) {
            System.out.print(prompt + ": ");
            String value = sc.next().trim();
            sc.nextLine();
            if (!value.isEmpty()) return value;
            System.out.println("  Input must not be blank.");
        }
    }
}
