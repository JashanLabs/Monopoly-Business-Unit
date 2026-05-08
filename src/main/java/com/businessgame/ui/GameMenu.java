package com.businessgame.ui;

import com.businessgame.service.AccountRegistry;
import com.businessgame.service.PropertyMarket;
import com.businessgame.service.SaveService;
import com.businessgame.service.StockMarket;
import com.businessgame.util.InputHelper;

/**
 * Top-level console menu and main game loop for the Business Game.
 *
 * <p>All dependencies are injected through the constructor so that each
 * subsystem remains independently testable.  The menu replaces the
 * original chain of {@code if (a == N)} branches with a {@code switch}
 * expression for clarity.</p>
 *
 * <p><b>Menu options</b></p>
 * <pre>
 *   0   Check balance
 *   1   Add balance (deposit)
 *   2   Withdraw funds
 *   3   Property dealing
 *   4   Transfer funds
 *   5   Stock market
 *   7   Save game to file
 *   9   Create new account
 *  11   End of round (apply inflation)
 *  99   Quit
 * </pre>
 */
public class GameMenu {

    private final AccountRegistry registry;
    private final PropertyMarket  propertyMarket;
    private final StockMarket     stockMarket;
    private final SaveService     saveService;
    private final InputHelper     input;

    // Composed UI handlers
    private final AccountUI  accountUI;
    private final PropertyUI propertyUI;
    private final MarketUI   marketUI;

    public GameMenu(AccountRegistry registry,
                    PropertyMarket  propertyMarket,
                    StockMarket     stockMarket,
                    SaveService     saveService,
                    InputHelper     input) {
        this.registry       = registry;
        this.propertyMarket = propertyMarket;
        this.stockMarket    = stockMarket;
        this.saveService    = saveService;
        this.input          = input;

        this.accountUI  = new AccountUI (registry, input);
        this.propertyUI = new PropertyUI(registry, propertyMarket, input);
        this.marketUI   = new MarketUI  (registry, stockMarket,    input);
    }

    // ------------------------------------------------------------------ //
    //  Main loop                                                           //
    // ------------------------------------------------------------------ //

    /**
     * Starts the game loop.  Runs until the user enters {@code 99}.
     *
     * <p>Market updates are flushed <em>before</em> each prompt so they
     * always appear between commands, never in the middle of user input.</p>
     */
    public void run() {
        printWelcome();
        boolean running = true;
        while (running) {
            // Show any market ticks that happened while the user was busy
            stockMarket.flushPendingUpdates();

            int choice = input.readInt("\nEnter command");
            switch (choice) {
                case  0 -> accountUI.checkBalance();
                case  1 -> accountUI.addBalance();
                case  2 -> accountUI.withdrawFunds();
                case  3 -> propertyUI.showMenu();
                case  4 -> accountUI.transferFunds();
                case  5 -> marketUI.showMenu();
                case  7 -> saveService.save(registry);
                case  9 -> accountUI.createAccount();
                case 11 -> endRound();
                case 99 -> { running = false; System.out.println("Thanks for playing!"); }
                default -> System.out.println("Unknown command — see the menu above.");
            }
        }
    }

    // ------------------------------------------------------------------ //
    //  Round management                                                    //
    // ------------------------------------------------------------------ //

    private void endRound() {
        propertyMarket.applyRoundInflation();
    }

    // ------------------------------------------------------------------ //
    //  Welcome screen                                                      //
    // ------------------------------------------------------------------ //

    private void printWelcome() {
        System.out.println();
        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║         BUSINESS GAME  v1.0          ║");
        System.out.println("╠══════════════════════════════════════╣");
        System.out.println("║   0  Check balance                   ║");
        System.out.println("║   1  Add balance                     ║");
        System.out.println("║   2  Withdraw funds                  ║");
        System.out.println("║   3  Property dealing                ║");
        System.out.println("║   4  Transfer funds                  ║");
        System.out.println("║   5  Stock market                    ║");
        System.out.println("║   7  Save game to file               ║");
        System.out.println("║   9  New account                     ║");
        System.out.println("║  11  End of round                    ║");
        System.out.println("║  99  Quit                            ║");
        System.out.println("╚══════════════════════════════════════╝");
    }
}
