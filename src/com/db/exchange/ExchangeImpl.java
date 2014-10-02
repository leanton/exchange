package com.db.exchange;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by krm on 30.09.14.
 */
// @ThreadSafe
class ExchangeImpl implements Exchange {
    private final Object lock = new Object();
    // @GuardedBy("lock")
    private final Map<String, Order> marketData;

    ExchangeImpl(Set<String> securities) {
        Map<String, Order> marketData = new HashMap<>();
        for (String security : securities) {
            double price = DefaultSecurityPrices.getDefaultPrice(security);
            marketData.put(security, new Order(price - 0.00001, price + 0.00001));
        }
        this.marketData = marketData;
    }

    @Override
    public Set<String> getAvailableSecurities() {
        synchronized(lock) {
            return marketData.keySet();
        }
    }

    @Override
    public double getMaxBid(String security) {
        synchronized(lock) {
            return marketData.get(security).bid;
        }
    }

    @Override
    public double getMinAsk(String security) {
        synchronized(lock) {
            return marketData.get(security).ask;
        }
    }

    @Override
    public boolean buy(String security, double price) {
        synchronized(lock) {
            Order order = marketData.get(security);
            if (order.ask <= price) {
                return true;
            }
            return false;
        }
    }

    @Override
    public boolean sell(String security, double price) {
        synchronized(lock) {
            Order order = marketData.get(security);
            if (order.bid >= price) {
                return true;
            }
            return false;
        }
    }

    void setNewPrices(String security, Order order) {
        synchronized(lock) {
            marketData.put(security, order);
        }
    }
}
