package com.businessgame.model;

/**
 * Represents a purchasable city/country property on the game board.
 *
 * <p>Each property has a display name, a base price, and a board index
 * that links it to the {@link Account} property-holdings array.</p>
 */
public class Property {

    private final int    index;
    private final String name;
    private       int    price;

    /**
     * @param index board index (0-based, matches Account's propertyHoldings array)
     * @param name  short city/country code displayed to players
     * @param price initial market price per unit
     */
    public Property(int index, String name, int price) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Property name must not be blank.");
        if (price <= 0)                     throw new IllegalArgumentException("Property price must be positive.");
        this.index = index;
        this.name  = name;
        this.price = price;
    }

    // ------------------------------------------------------------------ //
    //  Accessors                                                           //
    // ------------------------------------------------------------------ //

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    /**
     * Applies the end-of-round 5 % price increase.
     */
    public void applyRoundInflation() {
        price = (int) (price + price * 0.05);
    }

    /**
     * Directly sets the price (used for event-driven market changes).
     *
     * @param newPrice must be &gt; 0
     */
    public void setPrice(int newPrice) {
        if (newPrice <= 0) throw new IllegalArgumentException("Price must be positive.");
        this.price = newPrice;
    }

    @Override
    public String toString() {
        return String.format("%-6s  $%,d", name, price);
    }
}
