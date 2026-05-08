package com.businessgame.service;

import com.businessgame.model.Account;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Handles serialising game state to disk.
 *
 * <p>The original {@code saveToFile()} used raw {@link java.io.FileWriter}
 * without buffering and contained a Fernflower-generated try/catch ladder
 * that was both verbose and incorrect (catching {@link Throwable} as a
 * variable named {@code var0}).  This class replaces that with a clean
 * try-with-resources block and a {@link BufferedWriter} for efficiency.</p>
 */
public class SaveService {

    private static final String SAVE_FILE = "accounts.txt";

    /**
     * Writes all non-empty accounts to {@value #SAVE_FILE}.
     *
     * @param registry the live account registry
     */
    public void save(AccountRegistry registry) {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(SAVE_FILE))) {
            writer.write("# Business Game — saved at " + timestamp);
            writer.newLine();
            writer.write(String.format("# %-16s  %s%n", "AccountNumber", "Balance"));

            for (Account account : registry.allAccounts()) {
                writer.write(String.format("AccountNumber: %-16d  Balance: %.2f%n",
                        account.getAccountNumber(),
                        account.getBalance()));
            }

            System.out.println("Data saved to '" + SAVE_FILE + "' at " + timestamp + ".");
        } catch (IOException e) {
            System.err.println("ERROR: Could not save data — " + e.getMessage());
        }
    }
}
