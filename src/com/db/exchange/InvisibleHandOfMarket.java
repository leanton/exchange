package com.db.exchange;

import java.util.Set;

/**
 * Created by krm on 30.09.14.
 */
class InvisibleHandOfMarket implements Runnable {
    private final Set<ExchangeImpl> exchanges;

    InvisibleHandOfMarket(Set<ExchangeImpl> exchanges) {
        this.exchanges = exchanges;
    }

    @Override
    public void run() {
        for (String security : DefaultSecurityPrices.getAllSecurities()) {
            double sum = 0.0;
            for (Exchange exchange : exchanges) {
                sum += (exchange.getMaxBid(security) + exchange.getMinAsk(security)) / 2.0;
            }
            double meanPrice = sum / exchanges.size();
            for (ExchangeImpl exchange : exchanges) {
                double spread = (exchange.getMaxBid(security) + exchange.getMinAsk(security)) / 2.0;
                double bid = meanPrice - spread > 0 ? meanPrice - spread : 0.0;
                double ask = meanPrice + spread;
                Order order = new Order(bid, ask);
                exchange.setNewPrices(security, order);
            }
        }
    }
}
