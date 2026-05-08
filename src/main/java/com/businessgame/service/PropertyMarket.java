package com.businessgame.service;

import com.businessgame.model.Property;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Manages the game's property catalogue and applies round-end price inflation.
 *
 * <p>Properties are initialised from the hard-coded city data that was
 * originally scattered inside static initialisers.  The class is intentionally
 * package-level so that only services and UI layers interact with it.</p>
 */
public class PropertyMarket {

    private final List<Property> properties;

    public PropertyMarket() {
        properties = buildDefaultProperties();
    }

    // ------------------------------------------------------------------ //
    //  Default city catalogue                                              //
    // ------------------------------------------------------------------ //

    private static List<Property> buildDefaultProperties() {
        String[] names  = {
            "new", "rom", "hon", "syd", "dub", "kua", "deh", "roi",
            "ber", "rai", "zur", "ban", "tok", "lon", "cai", "wat",
            "tor", "mex", "sin", "par", "air", "auc", "cap", "seo",
            "mos", "roa"
        };
        int[] prices = {
            8500, 3000, 2500, 4500, 5000, 3500, 4500, 2500,
            5000, 9500, 5500, 2500, 3500, 4500, 2500, 7500,
            5000, 3500, 2500, 3500, 10500, 3500, 4500, 3000,
            5000, 5500
        };

        Property[] arr = new Property[names.length];
        for (int i = 0; i < names.length; i++) {
            arr[i] = new Property(i, names[i], prices[i]);
        }
        return Collections.unmodifiableList(Arrays.asList(arr));
    }

    // ------------------------------------------------------------------ //
    //  Queries                                                             //
    // ------------------------------------------------------------------ //

    /**
     * Finds a property by name (case-insensitive).
     *
     * @return {@link Optional} containing the matching property, or empty
     */
    public Optional<Property> findByName(String name) {
        return properties.stream()
                .filter(p -> p.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    /**
     * Returns an unmodifiable view of all properties.
     */
    public List<Property> getAllProperties() {
        return properties;
    }

    // ------------------------------------------------------------------ //
    //  Round management                                                    //
    // ------------------------------------------------------------------ //

    /**
     * Applies a 5 % end-of-round price increase to every property.
     * Called by the game loop when a player types {@code 11}.
     */
    public void applyRoundInflation() {
        properties.forEach(Property::applyRoundInflation);
        System.out.println("End of round: all property prices increased by 5 %.");
    }
}
