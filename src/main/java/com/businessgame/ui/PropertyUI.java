package com.businessgame.ui;

import com.businessgame.model.Account;
import com.businessgame.model.Property;
import com.businessgame.service.AccountRegistry;
import com.businessgame.service.PropertyMarket;
import com.businessgame.util.InputHelper;

import java.util.Optional;

/**
 * Console UI handler for all property-related operations:
 * view portfolio, check country price, buy property, and sell property.
 */
public class PropertyUI {

    private final AccountRegistry registry;
    private final PropertyMarket  market;
    private final InputHelper     input;

    public PropertyUI(AccountRegistry registry, PropertyMarket market, InputHelper input) {
        this.registry = registry;
        this.market   = market;
        this.input    = input;
    }

    // ------------------------------------------------------------------ //
    //  Sub-menu                                                            //
    // ------------------------------------------------------------------ //

    /**
     * Displays the property sub-menu and loops until the user chooses exit.
     */
    public void showMenu() {
        boolean active = true;
        while (active) {
            System.out.println();
            System.out.println("--- Property Dealing ---");
            System.out.println("  0  View portfolio");
            System.out.println("  1  Check country price");
            System.out.println("  2  Buy property");
            System.out.println("  3  Sell property");
            System.out.println("  4  Exit property dealing");

            int choice = input.readInt("Choice");
            switch (choice) {
                case 0 -> viewPortfolio();
                case 1 -> showCountryPrice();
                case 2 -> buyProperty();
                case 3 -> sellProperty();
                case 4 -> { active = false; System.out.println("Exited property dealing."); }
                default -> System.out.println("Unknown option — please try again.");
            }
        }
    }

    // ------------------------------------------------------------------ //
    //  View portfolio                                                      //
    // ------------------------------------------------------------------ //

    private void viewPortfolio() {
        long accountNumber = input.readLong("Enter account number");
        int  password      = input.readInt ("Enter password");

        Optional<Account> opt = registry.authenticate(accountNumber, password);
        if (opt.isEmpty()) {
            System.out.println("Invalid account number or password.");
            return;
        }
        Account account = opt.get();
        System.out.printf("%nPortfolio for account %d:%n", accountNumber);

        boolean hasAssets = false;
        for (Property prop : market.getAllProperties()) {
            int holding = account.getPropertyHolding(prop.getIndex());
            if (holding > 0) {
                System.out.printf("  %-6s  %d unit(s)  @  $%,d each%n",
                        prop.getName(), holding, prop.getPrice());
                hasAssets = true;
            }
        }
        if (!hasAssets) {
            System.out.println("  No properties owned.");
        }
    }

    // ------------------------------------------------------------------ //
    //  Price check                                                         //
    // ------------------------------------------------------------------ //

    private void showCountryPrice() {
        String name = input.readWord("Enter country name");
        market.findByName(name).ifPresentOrElse(
                p -> System.out.printf("Price of %-6s : $%,d%n", p.getName(), p.getPrice()),
                ()  -> System.out.println("Country not found.")
        );
    }

    // ------------------------------------------------------------------ //
    //  Buy                                                                 //
    // ------------------------------------------------------------------ //

    private void buyProperty() {
        long accountNumber = input.readLong("Enter account number");
        int  password      = input.readInt ("Enter password");

        Optional<Account> opt = registry.authenticate(accountNumber, password);
        if (opt.isEmpty()) {
            System.out.println("Invalid account number or password.");
            return;
        }
        Account account = opt.get();

        String countryName = input.readWord("Enter country name");
        Optional<Property> propOpt = market.findByName(countryName);
        if (propOpt.isEmpty()) {
            System.out.println("Country not found.");
            return;
        }
        Property property = propOpt.get();
        System.out.printf("Current price of %s: $%,d%n", property.getName(), property.getPrice());

        int units = input.readInt("Enter number of assets to buy");
        try {
            account.buyProperty(property.getIndex(), units, property.getPrice());
            System.out.printf("Purchase successful. Remaining balance: $%.2f%n",
                    account.getBalance());
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Purchase failed: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------ //
    //  Sell                                                                //
    // ------------------------------------------------------------------ //

    private void sellProperty() {
        long accountNumber = input.readLong("Enter account number");
        int  password      = input.readInt ("Enter password");

        Optional<Account> opt = registry.authenticate(accountNumber, password);
        if (opt.isEmpty()) {
            System.out.println("Invalid account number or password.");
            return;
        }
        Account account = opt.get();

        String countryName = input.readWord("Enter country name");
        Optional<Property> propOpt = market.findByName(countryName);
        if (propOpt.isEmpty()) {
            System.out.println("Country not found.");
            return;
        }
        Property property = propOpt.get();
        System.out.printf("Current price of %s: $%,d%n", property.getName(), property.getPrice());

        int units = input.readInt("Enter number of assets to sell");
        try {
            account.sellProperty(property.getIndex(), units, property.getPrice());
            System.out.printf("Sale successful. Current balance: $%.2f%n",
                    account.getBalance());
        } catch (IllegalArgumentException e) {
            System.out.println("Sale failed: " + e.getMessage());
        }
    }
}
