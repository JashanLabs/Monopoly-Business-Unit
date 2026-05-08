package com.businessgame.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages the global share price and runs a background thread that
 * randomly fluctuates the price every 10 seconds.
 *
 * <h2>Thread-safety model</h2>
 * <ul>
 *   <li>The share price is held in an {@link AtomicInteger}, so reads and
 *       writes are always consistent without locking.</li>
 *   <li>Price-history and the pending-notification queue are guarded by
 *       {@code this}, so both the fluctuator thread (writer) and the main
 *       game thread (reader/drainer) are safe.</li>
 * </ul>
 *
 * <h2>Non-interrupting update delivery</h2>
 * <p>Instead of printing directly to stdout (which would corrupt whatever
 * the user is currently reading or typing), the fluctuator silently enqueues
 * a {@link MarketUpdate} record on every tick.  The main game loop calls
 * {@link #flushPendingUpdates()} between commands, so market news appears
 * cleanly before the next prompt — never in the middle of one.</p>
 */
public class StockMarket {

    /** Initial share price. */
    private static final int  INITIAL_PRICE    = 10;
    /** Maximum single-tick price change (+/-). */
    private static final int  MAX_FLUCTUATION  = 10;
    /** Milliseconds between automatic price ticks. */
    private static final long TICK_INTERVAL_MS = 10_000L;
    /** Number of price history entries kept for the chart. */
    private static final int  HISTORY_SIZE     = 50;
    /** Minimum allowed price — prevents the stock reaching zero. */
    private static final int  MIN_PRICE        = 1;

    // ── State ─────────────────────────────────────────────────────────── //

    private final AtomicInteger price        = new AtomicInteger(INITIAL_PRICE);
    private final int[]         priceHistory = new int[HISTORY_SIZE];
    private       int           historyIndex = 0;

    /** Queued ticks waiting to be shown to the user. Guarded by {@code this}. */
    private final List<MarketUpdate> pendingUpdates = new ArrayList<>();

    private Thread          fluctuatorThread;
    private volatile boolean running = false;

    // ── Public accessors ──────────────────────────────────────────────── //

    public int getCurrentPrice() {
        return price.get();
    }

    /**
     * Returns a snapshot of the price history array (copy, safe to read
     * without holding the lock).
     */
    public synchronized int[] getPriceHistorySnapshot() {
        return priceHistory.clone();
    }

    // ── Pending-update queue ──────────────────────────────────────────── //

    /**
     * Prints all market updates that accumulated since the last call, then
     * clears the queue.  Call this from the main thread between commands so
     * output never interleaves with user prompts.
     *
     * <p>If there are no pending updates the method returns silently.</p>
     */
    public synchronized void flushPendingUpdates() {
        if (pendingUpdates.isEmpty()) return;

        System.out.println();
        System.out.println("┌─── Market News ──────────────────────┐");
        for (MarketUpdate u : pendingUpdates) {
            System.out.printf("│  Price: $%-5d  Change: %+d%n", u.newPrice(), u.delta());
        }
        System.out.println("└──────────────────────────────────────┘");

        // Show the latest chart snapshot
        StockChart.print(priceHistory);

        pendingUpdates.clear();
    }

    // ── Background fluctuator ─────────────────────────────────────────── //

    /**
     * Starts the background market-fluctuation thread.
     * Safe to call only once; subsequent calls are ignored.
     */
    public synchronized void startFluctuator() {
        if (running) return;
        running          = true;
        fluctuatorThread = new Thread(this::fluctuate, "MarketFluctuator");
        fluctuatorThread.setDaemon(true); // does not block JVM shutdown
        fluctuatorThread.start();
    }

    /**
     * Signals the fluctuator thread to stop and waits for it to terminate.
     */
    public synchronized void stopFluctuator() {
        running = false;
        if (fluctuatorThread != null) {
            fluctuatorThread.interrupt();
        }
    }

    private void fluctuate() {
        Random rng = new Random();
        while (running) {
            try {
                Thread.sleep(TICK_INTERVAL_MS);

                int delta    = rng.nextInt(MAX_FLUCTUATION * 2 + 1) - MAX_FLUCTUATION;
                int newPrice = Math.max(MIN_PRICE, price.get() + delta);
                price.set(newPrice);

                // Record tick — guarded so the main thread sees a consistent state
                synchronized (this) {
                    priceHistory[historyIndex] = newPrice;
                    historyIndex = (historyIndex + 1) % HISTORY_SIZE;
                    pendingUpdates.add(new MarketUpdate(newPrice, delta));
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    // ── Inner record ─────────────────────────────────────────────────── //

    /** Immutable snapshot of one market tick. */
    private record MarketUpdate(int newPrice, int delta) {}
}
