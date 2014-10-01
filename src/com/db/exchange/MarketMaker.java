package com.db.exchange;

import java.util.Random;

/**
 * Created by krm on 30.09.14.
 */
class MarketMaker implements Runnable {
    private static final double BID_FR = 1.3;
    private static final double ASK_FR = 1.7;
    private final Random rnd;
    private final ExchangeImpl exchange;
    private long startTime;

    MarketMaker(ExchangeImpl exchange, int seed) {
        this.exchange = exchange;
        this.rnd = new Random(seed);
    }

    @Override
    public void run() {
        startTime = System.currentTimeMillis();
        for (String security : exchange.getAvailableSecurities()) {
            double price = calcPrice(security);
            double bidDelta = generateBidDelta();
            double askDelta = generateAskDelta();
            Order order = new Order(price + bidDelta, price + askDelta);
            exchange.setNewPrices(security, order);
            Thread.yield();
        }
    }

    private double calcPrice(String security) {
        return (exchange.getMaxBid(security) + exchange.getMinAsk(security)) / 2.0;
    }

    private double generateBidDelta() {
        return - Math.abs(Math.sin(t() * BID_FR) * (rnd.nextDouble() % 0.001) + 1e-7);
    }

    private double generateAskDelta() {
        return Math.abs(Math.sin(t() * ASK_FR) * (rnd.nextDouble() % 0.001) + 1e-7);
    }

    private double t() {
        return System.currentTimeMillis() - startTime;
    }
}
