package com.db.trader;

import com.db.exchange.Exchange;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class SuperHFTArbitrageBotEdu {
    private final Set<? extends Exchange> exchanges; // Set of all exchanges, all exchanges have same securities
    private final Map<String, ExchangeInfo> maxBids;
    private final Map<String, ExchangeInfo> minAsks;
    private final long timeToWorkMillis;
    private double balance;

    /**
     * Конструктор
     * */
    public SuperHFTArbitrageBotEdu(Set<? extends Exchange> exchanges, double balance, long timeToWork, TimeUnit unit) {
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

    public void arbitrage(Set<? extends Exchange> exchanges) {
        for (Exchange exchange : exchanges) {

            Set<String> securities = exchange.getAvailableSecurities();
            for (String security : securities) {

                double bid = exchange.getMaxBid(security);
                double ask = exchange.getMinAsk(security);

                ExchangeInfo maxBidInfo = maxBids.get(security);
                double maxBid = maxBidInfo.price;
                Exchange expensiveExchange = maxBidInfo.exchange;

                if (ask > maxBid) {

                    trade(expensiveExchange, exchange, security, maxBid, ask);

                } else if (bid > maxBid) {

                    updateMaxBid(exchange, security, bid);

                }

                ExchangeInfo minAskInfo = minAsks.get(security);
                double minAsk = minAskInfo.price;
                Exchange cheapExchange = minAskInfo.exchange;

                if (bid > minAsk) {

                    trade(exchange, cheapExchange, security, bid, minAsk);

                } else if (ask < minAsk) {

                    updateMinAsk(exchange, security, ask);

                }

            }
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

    /**
    * Проверяет, можно ли купить акцию security у биржи exchange по цене ask
    * */
    private boolean buy(Exchange exchange, String security, double ask) {
        if (exchange.buy(security, ask)) {
            balance -= ask;
            return true;
        }
        return false;
    }

    /**
     * Проверяет, можно ли продать акцию security у биржи exchange по цене bid
     * */
    private boolean sell(Exchange exchange, String security, double bid) {
        if (exchange.sell(security, bid)) {
            balance += bid;
            return true;
        }
        return false;
    }

    /**
     * Вложенный класс с информацией о бирже
     * */
    private class ExchangeInfo {
        Exchange exchange;
        double price;
        private ExchangeInfo(Exchange exchange, double price) {
            this.exchange = exchange;
            this.price = price;
        }
    }

}
