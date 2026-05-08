package com.businessgame.ui;

import com.businessgame.model.Account;
import com.businessgame.service.AccountRegistry;
import com.businessgame.service.StockChart;
import com.businessgame.service.StockMarket;
import com.businessgame.util.InputHelper;

import java.util.Optional;

/**
 * Console UI handler for stock-market trading:
 * check price, buy shares, and sell shares.
 */
public class MarketUI {

    private final AccountRegistry registry;
    private final StockMarket     stockMarket;
    private final InputHelper     input;

    public MarketUI(AccountRegistry registry, StockMarket stockMarket, InputHelper input) {
        this.registry    = registry;
        this.stockMarket = stockMarket;
        this.input       = input;
    }

    // ------------------------------------------------------------------ //
    //  Sub-menu                                                            //
    // ------------------------------------------------------------------ //

    /**
     * Displays the market sub-menu and loops until the user chooses exit.
     *
     * <p>Pending market updates are flushed before each inner prompt for the
     * same reason as the top-level loop — so ticks never corrupt user input.</p>
     */
    public void showMenu() {
        boolean active = true;
        while (active) {
            // Flush any ticks that arrived while the last operation ran
            stockMarket.flushPendingUpdates();

            System.out.println();
            System.out.println("--- Stock Market ---");
            System.out.println("  0  Check current price & chart");
            System.out.println("  1  Buy shares");
            System.out.println("  2  Sell shares");
            System.out.println("  3  Exit market");

            int choice = input.readInt("Choice");
            switch (choice) {
                case 0 -> checkPrice();
                case 1 -> buyShares();
                case 2 -> sellShares();
                case 3 -> { active = false; System.out.println("Exiting market."); }
                default -> System.out.println("Unknown option — please try again.");
            }
        }
    }

    // ------------------------------------------------------------------ //
    //  Operations                                                          //
    // ------------------------------------------------------------------ //

    private void checkPrice() {
        System.out.printf("Current price per share: $%d%n", stockMarket.getCurrentPrice());
        StockChart.print(stockMarket.getPriceHistorySnapshot());
    }

    private void buyShares() {
        long accountNumber = input.readLong("Enter account number");
        int  password      = input.readInt ("Enter password");

        Optional<Account> opt = registry.authenticate(accountNumber, password);
        if (opt.isEmpty()) {
            System.out.println("Invalid account number or password.");
            return;
        }
        Account account = opt.get();

        int price  = stockMarket.getCurrentPrice();
        int shares = input.readInt("Enter number of shares to buy");

        System.out.printf("Price per share: $%d  |  Total cost: $%,d%n", price, price * shares);
        try {
            account.buyShares(shares, price);
            System.out.printf("Purchase successful. Remaining balance: $%.2f%n",
                    account.getBalance());
        } catch (IllegalStateException e) {
            System.out.println("Purchase failed: " + e.getMessage());
        }
    }

    private void sellShares() {
        long accountNumber = input.readLong("Enter account number");
        int  password      = input.readInt ("Enter password");

        Optional<Account> opt = registry.authenticate(accountNumber, password);
        if (opt.isEmpty()) {
            System.out.println("Invalid account number or password.");
            return;
        }
        Account account = opt.get();

        int price  = stockMarket.getCurrentPrice();
        int shares = input.readInt("Enter number of shares to sell");

        try {
            account.sellShares(shares, price);
            System.out.printf("Sale successful. Received: $%,d  |  Current balance: $%.2f%n",
                    price * shares, account.getBalance());
        } catch (IllegalArgumentException e) {
            System.out.println("Sale failed: " + e.getMessage());
        }
    }
}
