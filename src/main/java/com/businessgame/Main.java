package com.businessgame;

import com.businessgame.service.AccountRegistry;
import com.businessgame.service.PropertyMarket;
import com.businessgame.service.SaveService;
import com.businessgame.service.StockMarket;
import com.businessgame.ui.GameMenu;
import com.businessgame.util.InputHelper;

import java.util.Scanner;

/**
 * Application entry point for the Business Game.
 *
 * <p>Bootstraps all services, wires dependencies, registers a shutdown hook
 * to stop the market-fluctuator thread cleanly, and hands control to the
 * {@link GameMenu} main loop.</p>
 */
public class Main {

    public static void main(String[] args) {
        // ── Services ─────────────────────────────────────────────────────
        AccountRegistry registry       = new AccountRegistry();
        PropertyMarket  propertyMarket = new PropertyMarket();
        StockMarket     stockMarket    = new StockMarket();
        SaveService     saveService    = new SaveService();
        InputHelper     inputHelper    = new InputHelper(new Scanner(System.in));

        // ── Start background market thread ────────────────────────────────
        stockMarket.startFluctuator();

        // ── Graceful shutdown hook ────────────────────────────────────────
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            stockMarket.stopFluctuator();
            System.out.println("\n[System] Market fluctuator stopped. Goodbye.");
        }, "ShutdownHook"));

        // ── Launch game ───────────────────────────────────────────────────
        GameMenu menu = new GameMenu(registry, propertyMarket, stockMarket, saveService, inputHelper);
        menu.run();
    }
}
