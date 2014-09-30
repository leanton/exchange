package com.db.trader;

import com.db.exchange.Exchange;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by krm on 30.09.14.
 */
public class SuperHFTArbitragerBot {
    private final Set<? extends Exchange> exchanges;
    private final Map<String, Pair> maxBids;
    private final Map<String, Pair> minAsks;
    private double balance;

    public SuperHFTArbitragerBot(Set<? extends Exchange> exchanges, double initialBalance) {
        this.balance = initialBalance;
        this.exchanges = exchanges;
        Exchange exchange = exchanges.iterator().next();
        HashMap<String, Pair> bids = new HashMap<>();
        HashMap<String, Pair> asks = new HashMap<>();
        for (String security : exchange.getAvailableSecurities()) {
            bids.put(security, new Pair(exchange, 0.0));
            asks.put(security, new Pair(exchange, Double.MAX_VALUE));
        }
        this.maxBids = Collections.unmodifiableMap(bids);
        this.minAsks = Collections.unmodifiableMap(asks);
    }

    public void start() {
        new Thread(this::startTrading).start();
    }

    private void startTrading() {
        System.out.println("Trading started with " + balance + "! Good luck!");
        for (int i = 0; i < 100_000; ++i) {
            arbitrage(exchanges);
        }
        System.out.println("Your balance is " + balance);
    }

    private void arbitrage(Set<? extends Exchange> exchanges) {
        for (Exchange exchange : exchanges) {
            Set<String> securities = exchange.getAvailableSecurities();
            for (String security : securities) {
                processSecurity(exchange, security);
            }
        }
    }

    private void processSecurity(Exchange exchange, String security) {
        double bid = exchange.getMaxBid(security);
        double ask = exchange.getMinAsk(security);
        playOnMaxBid(exchange, security, bid, ask);
        playOnMinAsk(exchange, security, bid, ask);
    }

    private void playOnMaxBid(Exchange exchange, String security, double bid, double ask) {
        Pair bidPair = maxBids.get(security);
        double maxBid = bidPair.price;
        Exchange expensiveExchange = bidPair.exchange;
        if (maxBid > ask) {
            trade(expensiveExchange, exchange, security, maxBid, ask);
        } else if (maxBid < bid) {
            updateMaxBid(exchange, security, bid);
        }
    }

    private void playOnMinAsk(Exchange exchange, String security, double bid, double ask) {
        Pair askPair = minAsks.get(security);
        double minAsk = askPair.price;
        Exchange cheapExchange = askPair.exchange;
        if (minAsk < bid) {
            trade(exchange, cheapExchange, security, bid, minAsk);
        } else if (minAsk > ask) {
            updateMinAsk(exchange, security, ask);
        }
    }

    private void trade(Exchange expensiveExchange, Exchange cheapExchange, String security, double bid, double ask) {
        if (buy(cheapExchange, security, ask)) {
            if (sell(expensiveExchange, security, bid)) {
                System.out.println("Gotcha! " + security + " bid " + bid + " ask " + ask);
            } else {
                System.out.println("Oops! Just lost " + ask);
            }
        }
    }

    private void updateMinAsk(Exchange exchange, String security, double ask) {
        minAsks.get(security).price = ask;
        minAsks.get(security).exchange = exchange;
    }

    private void updateMaxBid(Exchange exchange, String security, double bid) {
        maxBids.get(security).price = bid;
        maxBids.get(security).exchange = exchange;
    }

    private boolean buy(Exchange cheapExchange, String security, double ask) {
        if (cheapExchange.buy(security, ask)) {
            balance -= ask;
            return true;
        }
        return false;
    }

    private boolean sell(Exchange exchange, String security, double bid) {
        if (exchange.sell(security, bid)) {
            balance += bid;
            return true;
        }
        return false;
    }

    private class Pair {
        Exchange exchange;
        double price;
        private Pair(Exchange exchange, double price) {
            this.exchange = exchange;
            this.price = price;
        }
    }
}
