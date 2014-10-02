package com.db.trader;

import com.db.exchange.Exchange;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by krm on 30.09.14.
 */
public class SuperHFTArbitragerBot {
    private final Set<? extends Exchange> exchanges;
    private final Map<String, ExchangeInfo> maxBids;
    private final Map<String, ExchangeInfo> minAsks;
    private final long timeToWorkMillis;
    private double balance;

    public SuperHFTArbitragerBot(Set<? extends Exchange> exchanges, double balance, long timeToWork, TimeUnit unit) {
        this.balance = balance;
        this.timeToWorkMillis = unit.toMillis(timeToWork);
        this.exchanges = exchanges;
        Exchange exchange = exchanges.iterator().next();
        HashMap<String, ExchangeInfo> bids = new HashMap<>();
        HashMap<String, ExchangeInfo> asks = new HashMap<>();
        for (String security : exchange.getAvailableSecurities()) {
            bids.put(security, new ExchangeInfo(exchange, 0.0));
            asks.put(security, new ExchangeInfo(exchange, Double.MAX_VALUE));
        }
        this.maxBids = Collections.unmodifiableMap(bids);
        this.minAsks = Collections.unmodifiableMap(asks);
    }

    public double getBalance() {
        return balance;
    }

    public void startTrading() {
        System.out.println("Start trading!");
        long timeToStop = System.currentTimeMillis() + timeToWorkMillis;
        while (System.currentTimeMillis() < timeToStop) {
            arbitrage(exchanges);
        }
        System.out.println("Your current balance is " + balance);
    }

    public void arbitrage(Set<? extends Exchange> exchanges) {
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
        ExchangeInfo maxBidInfo = maxBids.get(security);
        double maxBid = maxBidInfo.price;
        Exchange expensiveExchange = maxBidInfo.exchange;
        if (maxBid > ask) {
            trade(expensiveExchange, exchange, security, maxBid, ask);
        } else if (maxBid < bid) {
            updateMaxBid(exchange, security, bid);
        }
    }

    private void playOnMinAsk(Exchange exchange, String security, double bid, double ask) {
        ExchangeInfo minAskInfo = minAsks.get(security);
        double minAsk = minAskInfo.price;
        Exchange cheapExchange = minAskInfo.exchange;
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

    private class ExchangeInfo {
        Exchange exchange;
        double price;
        private ExchangeInfo(Exchange exchange, double price) {
            this.exchange = exchange;
            this.price = price;
        }
    }
}
