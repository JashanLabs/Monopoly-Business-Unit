package com.businessgame.service;

import com.businessgame.model.Account;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * In-memory registry that manages all player {@link Account} objects.
 *
 * <p>Replaces the original parallel arrays ({@code accNum[]}, {@code pass[]},
 * {@code np[]}, {@code sh[]}, {@code assets[][]}) with a type-safe
 * {@link Map} keyed by account number.  All mutation is encapsulated here;
 * callers never touch the underlying collection directly.</p>
 *
 * <p>The registry supports up to {@value #MAX_ACCOUNTS} accounts, matching
 * the original hard-coded array sizes.  This limit can be removed by
 * switching the backing store to a {@link java.util.LinkedHashMap}.</p>
 */
public class AccountRegistry {

    public static final int MAX_ACCOUNTS = 100;

    /** accountNumber → Account */
    private final Map<Long, Account> accounts = new HashMap<>(MAX_ACCOUNTS);

    // ------------------------------------------------------------------ //
    //  Account creation                                                    //
    // ------------------------------------------------------------------ //

    /**
     * Registers a new account.
     *
     * @param accountNumber player-chosen identifier
     * @param password      PIN (stored as-is; no hashing in this demo game)
     * @throws IllegalStateException    if the registry is full
     * @throws IllegalArgumentException if the account number is already taken
     */
    public void createAccount(long accountNumber, int password) {
        if (accounts.size() >= MAX_ACCOUNTS) {
            throw new IllegalStateException("Maximum number of accounts reached (" + MAX_ACCOUNTS + ").");
        }
        if (accounts.containsKey(accountNumber)) {
            throw new IllegalArgumentException("Account " + accountNumber + " already exists.");
        }
        accounts.put(accountNumber, new Account(accountNumber, password));
        System.out.println("Account created successfully.");
    }

    // ------------------------------------------------------------------ //
    //  Lookup helpers                                                      //
    // ------------------------------------------------------------------ //

    /**
     * Retrieves an account by number without authentication.
     *
     * @return {@link Optional} containing the account, or empty if not found
     */
    public Optional<Account> find(long accountNumber) {
        return Optional.ofNullable(accounts.get(accountNumber));
    }

    /**
     * Retrieves an account after verifying the password.
     *
     * @return {@link Optional} containing the account if credentials match,
     *         or empty otherwise
     */
    public Optional<Account> authenticate(long accountNumber, int password) {
        return find(accountNumber)
                .filter(a -> a.authenticate(password));
    }

    /**
     * Returns a view of all registered accounts (read-only iteration).
     */
    public Iterable<Account> allAccounts() {
        return accounts.values();
    }

    public int size() {
        return accounts.size();
    }
}
