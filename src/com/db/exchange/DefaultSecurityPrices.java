package com.db.exchange;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by krm on 30.09.14.
 */
// @Immutable
class DefaultSecurityPrices {
    private static final Map<String, Double> DEFAULT_PRICES;
    static {
        Map<String, Double> prices = new HashMap<>();
        prices.put("EURUSD", 1.2);
        prices.put("EURRUB", 45.8);
        prices.put("AAPL", 100.75);
        prices.put("B", 1.1);
        prices.put("CCMP", 4.493);
        prices.put("D", 1.73);
        prices.put("E", 1.24);
        prices.put("F", 1.26);
        prices.put("G", 0.54);
        prices.put("H", 1.97);
        prices.put("I", 1.45);
        prices.put("J", 1.3);
        prices.put("K", 1.12);
        prices.put("L", 1.25);
        prices.put("M", 1.34);
        prices.put("N", 1.82);
        prices.put("O", 1.61);
        prices.put("P", 1.45);
        prices.put("Q", 1.77);
        prices.put("R", 1.28);
        DEFAULT_PRICES = Collections.unmodifiableMap(prices);
    }

    static double getDefaultPrice(String security) {
        return DEFAULT_PRICES.get(security);
    }

    static Set<String> getAllSecurities() {
        return DEFAULT_PRICES.keySet();
    }
}
