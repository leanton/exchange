package com.db.exchange;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by krm on 30.09.14.
 */
class MarketMaker implements Runnable {
    private static final double BID_FR = 1.3;
    private static final double ASK_FR = 1.7;
    private final ExchangeImpl exchange;

    MarketMaker(ExchangeImpl exchange) {
        this.exchange = exchange;
    }

    @Override
    public void run() {
        for (String security : exchange.getAvailableSecurities()) {
            double price = calcPrice(security);
            double bidDelta = generateBidDelta();
            double askDelta = generateAskDelta();
            Order order = new Order(price + bidDelta, price + askDelta);
            exchange.setNewPrices(security, order);
        }
    }

    private double calcPrice(String security) {
        return (exchange.getMaxBid(security) + exchange.getMinAsk(security)) / 2.0;
    }

    private double generateBidDelta() {
        return - Math.abs(Math.sin(t() * BID_FR) * (ThreadLocalRandom.current().nextDouble(0.001) + 0.00001));
    }

    private double generateAskDelta() {
        return Math.abs(Math.sin(t() * ASK_FR) * (ThreadLocalRandom.current().nextDouble(0.001) + 0.00001));
    }

    private double t() {
        return System.currentTimeMillis();
    }
}
