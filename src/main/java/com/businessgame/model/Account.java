package com.businessgame.model;

/**
 * Represents a player's bank account in the Business Game.
 *
 * <p>Stores account credentials, balance, share holdings, and property assets.
 * All financial amounts are tracked in double for precision during
 * percentage-based calculations (e.g. 10% deposit bonus on addBalance).</p>
 */
public class Account {

    /** Maximum number of distinct properties (countries) supported. */
    public static final int MAX_PROPERTIES = 26;

    private final long accountNumber;
    private final int  password;
    private double     balance;
    private int        shares;

    /**
     * assets[j] = number of units owned in country at index j.
     * Indexed by {@link com.businessgame.model.Property#getIndex()}.
     */
    private final int[] propertyHoldings;

    /**
     * @param accountNumber unique identifier chosen by the player
     * @param password       PIN used to authenticate sensitive operations
     */
    public Account(long accountNumber, int password) {
        this.accountNumber   = accountNumber;
        this.password        = password;
        this.balance         = 0.0;
        this.shares          = 0;
        this.propertyHoldings = new int[MAX_PROPERTIES];
    }

    // ------------------------------------------------------------------ //
    //  Credentials                                                         //
    // ------------------------------------------------------------------ //

    public long getAccountNumber() {
        return accountNumber;
    }

    /**
     * Validates a supplied PIN without exposing the stored value.
     *
     * @param candidate PIN entered by the user
     * @return {@code true} if the PIN matches
     */
    public boolean authenticate(int candidate) {
        return this.password == candidate;
    }

    // ------------------------------------------------------------------ //
    //  Balance                                                             //
    // ------------------------------------------------------------------ //

    public double getBalance() {
        return balance;
    }

    /**
     * Deposits {@code amount} plus a 10 % bonus into the account.
     *
     * @param amount base amount to deposit (must be &gt; 0)
     * @throws IllegalArgumentException if amount is not positive
     */
    public void deposit(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Deposit amount must be positive.");
        balance += amount + amount * 0.10;
    }

    /**
     * Withdraws exactly {@code amount} from the account.
     *
     * @param amount amount to withdraw (must be &gt; 0)
     * @throws IllegalArgumentException if amount is not positive
     * @throws IllegalStateException    if balance is insufficient
     */
    public void withdraw(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Withdrawal amount must be positive.");
        if (balance < amount) throw new IllegalStateException("Insufficient balance.");
        balance -= amount;
    }

    // ------------------------------------------------------------------ //
    //  Share trading                                                       //
    // ------------------------------------------------------------------ //

    public int getShares() {
        return shares;
    }

    /**
     * Purchases {@code quantity} shares at {@code pricePerShare}.
     *
     * @throws IllegalStateException if balance is insufficient
     */
    public void buyShares(int quantity, int pricePerShare) {
        double cost = (double) quantity * pricePerShare;
        if (balance < cost) throw new IllegalStateException("Insufficient balance to buy shares.");
        shares  += quantity;
        balance -= cost;
    }

    /**
     * Sells {@code quantity} shares at {@code pricePerShare}.
     *
     * @throws IllegalArgumentException if the player does not hold enough shares
     */
    public void sellShares(int quantity, int pricePerShare) {
        if (quantity > shares) throw new IllegalArgumentException("You don't own enough shares to sell.");
        shares  -= quantity;
        balance += (double) quantity * pricePerShare;
    }

    // ------------------------------------------------------------------ //
    //  Property holdings                                                   //
    // ------------------------------------------------------------------ //

    /**
     * Returns the number of units held in the property at {@code propertyIndex}.
     */
    public int getPropertyHolding(int propertyIndex) {
        return propertyHoldings[propertyIndex];
    }

    /**
     * Purchases {@code units} of the property at {@code propertyIndex}
     * at {@code unitPrice} each.
     *
     * @throws IllegalStateException if balance is insufficient
     */
    public void buyProperty(int propertyIndex, int units, int unitPrice) {
        double cost = (double) units * unitPrice;
        if (balance < cost) throw new IllegalStateException("Insufficient balance to buy property.");
        propertyHoldings[propertyIndex] += units;
        balance -= cost;
    }

    /**
     * Sells {@code units} of the property at {@code propertyIndex}
     * at {@code unitPrice} each.
     *
     * @throws IllegalArgumentException if the player does not hold enough units
     */
    public void sellProperty(int propertyIndex, int units, int unitPrice) {
        if (propertyHoldings[propertyIndex] < units) {
            throw new IllegalArgumentException("You don't own enough assets to sell.");
        }
        propertyHoldings[propertyIndex] -= units;
        balance += (double) units * unitPrice;
    }
}
