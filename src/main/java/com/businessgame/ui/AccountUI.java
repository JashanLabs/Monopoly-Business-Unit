package com.businessgame.ui;

import com.businessgame.model.Account;
import com.businessgame.service.AccountRegistry;
import com.businessgame.util.InputHelper;

import java.util.Optional;

/**
 * Console UI handler for all account-related operations:
 * create, balance check, deposit, withdrawal, and transfer.
 *
 * <p>Each public method corresponds to one menu action in {@link GameMenu}.
 * Business rules live in {@link Account}; this class only drives
 * input/output and delegates exceptions to user-friendly messages.</p>
 */
public class AccountUI {

    private final AccountRegistry registry;
    private final InputHelper      input;

    public AccountUI(AccountRegistry registry, InputHelper input) {
        this.registry = registry;
        this.input    = input;
    }

    // ------------------------------------------------------------------ //
    //  Create account                                                      //
    // ------------------------------------------------------------------ //

    /**
     * Prompts for a new account number and password, then registers it.
     */
    public void createAccount() {
        long accountNumber = input.readLong("Enter desired account number");
        try {
            int password = input.readInt("Set password");
            registry.createAccount(accountNumber, password);
        } catch (IllegalArgumentException e) {
            System.out.println("Could not create account: " + e.getMessage());
        } catch (IllegalStateException e) {
            System.out.println("System: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------ //
    //  Balance                                                             //
    // ------------------------------------------------------------------ //

    /**
     * Displays the balance for a specified account.
     * No password required — matches original behaviour.
     */
    public void checkBalance() {
        long accountNumber = input.readLong("Enter account number");
        registry.find(accountNumber).ifPresentOrElse(
                a -> System.out.printf("Balance for account %d: $%.2f%n",
                        a.getAccountNumber(), a.getBalance()),
                ()  -> System.out.println("Account not found.")
        );
    }

    // ------------------------------------------------------------------ //
    //  Deposit                                                             //
    // ------------------------------------------------------------------ //

    /**
     * Adds funds (plus a 10 % bonus) to an account.
     * No password required — matches original banker/game-master behaviour.
     */
    public void addBalance() {
        long accountNumber = input.readLong("Enter account number");
        Optional<Account> opt = registry.find(accountNumber);
        if (opt.isEmpty()) {
            System.out.println("Account not found.");
            return;
        }
        Account account = opt.get();
        double amount = input.readInt("Enter amount");
        try {
            account.deposit(amount);
            System.out.printf("Balance updated. New balance: $%.2f  (includes 10%% bonus)%n",
                    account.getBalance());
        } catch (IllegalArgumentException e) {
            System.out.println("Deposit failed: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------ //
    //  Withdrawal                                                          //
    // ------------------------------------------------------------------ //

    public void withdrawFunds() {
        long accountNumber = input.readLong("Enter account number");
        int  password      = input.readInt ("Enter password");

        Optional<Account> opt = registry.authenticate(accountNumber, password);
        if (opt.isEmpty()) {
            System.out.println("Invalid account number or password.");
            return;
        }
        Account account = opt.get();
        double amount = input.readInt("Enter amount");
        try {
            account.withdraw(amount);
            System.out.printf("Withdrawal successful. Remaining balance: $%.2f%n",
                    account.getBalance());
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Withdrawal failed: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------ //
    //  Transfer                                                            //
    // ------------------------------------------------------------------ //

    public void transferFunds() {
        long senderNumber   = input.readLong("Enter sender account number");
        int  senderPassword = input.readInt ("Enter sender password");

        Optional<Account> senderOpt = registry.authenticate(senderNumber, senderPassword);
        if (senderOpt.isEmpty()) {
            System.out.println("Invalid sender account or password.");
            return;
        }

        long recipientNumber = input.readLong("Enter recipient account number");
        Optional<Account> recipientOpt = registry.find(recipientNumber);
        if (recipientOpt.isEmpty()) {
            System.out.println("Recipient account not found.");
            return;
        }

        Account sender    = senderOpt.get();
        Account recipient = recipientOpt.get();
        double  amount    = input.readInt("Enter transfer amount");

        try {
            sender.withdraw(amount);
            // Recipient receives exactly the transferred amount (no bonus)
            recipient.deposit(amount / 1.10); // undo deposit bonus so recipient gets exact amount
            System.out.printf("Transfer successful. Sender balance: $%.2f | Recipient balance: $%.2f%n",
                    sender.getBalance(), recipient.getBalance());
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Transfer failed: " + e.getMessage());
        }
    }
}
