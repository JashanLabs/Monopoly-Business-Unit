package com.businessgame.service;

/**
 * Renders a simple ASCII bar chart of recent stock-price history.
 *
 * <p>Extracted from the inner {@code MarketFluctuator} class in the original
 * source so that charting logic can be tested and reused independently.</p>
 */
public final class StockChart {

    private StockChart() { /* utility class — no instances */ }

    /**
     * Prints a vertical bar chart to {@link System#out}.
     *
     * <p>Each column represents one historical data point.  Bars are drawn
     * with {@code *} characters; empty cells with spaces.  Y-axis steps
     * by 2 to keep output compact.  A zero entry means "no data yet" and
     * is rendered as an empty column.</p>
     *
     * @param stockPrices array of historical prices; may contain zeros for
     *                    positions not yet populated
     */
    public static void print(int[] stockPrices) {
        int maxPrice = 0;
        for (int price : stockPrices) {
            if (price > maxPrice) maxPrice = price;
        }

        if (maxPrice == 0) return; // nothing to draw yet

        System.out.println();
        System.out.println("=== Stock Price Chart ===");
        for (int i = maxPrice; i >= 0; i -= 2) {
            System.out.printf("%4d | ", i);
            for (int price : stockPrices) {
                System.out.print(price >= i ? "* " : "  ");
            }
            System.out.println();
        }

        System.out.print("     +");
        for (int ignored : stockPrices) System.out.print("--");
        System.out.println();

        System.out.print("      ");
        for (int i = 0; i < stockPrices.length; i++) {
            System.out.printf("%2d", i % 10); // keep axis readable
        }
        System.out.println();
    }
}
