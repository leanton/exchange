package com.db.exchange;

import com.db.trader.SuperHFTArbitragerBot;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by krm on 30.09.14.
 */
public class StartMarketDay {
    private static final ScheduledExecutorService executor =
            Executors.newScheduledThreadPool(5, r -> {
                Thread t = new Thread(r);
                t.setDaemon(true);
                return t;
            });

    public static void main(String[] args) {
        Set<ExchangeImpl> exchanges = new HashSet<>();
        exchanges.add(new ExchangeImpl(DefaultSecurityPrices.getAllSecurities()));
        exchanges.add(new ExchangeImpl(DefaultSecurityPrices.getAllSecurities()));
        exchanges.add(new ExchangeImpl(DefaultSecurityPrices.getAllSecurities()));
        exchanges.add(new ExchangeImpl(DefaultSecurityPrices.getAllSecurities()));
        int i = 0;
        for (ExchangeImpl exchange : exchanges) {
            executor.scheduleAtFixedRate(new MarketMaker(exchange, ++i), 0, 50, TimeUnit.MILLISECONDS);
        }
        executor.scheduleAtFixedRate(new InvisibleHandOfMarket(exchanges), 0, 50, TimeUnit.MILLISECONDS);
        new SuperHFTArbitragerBot(exchanges, 1000.0, 5, TimeUnit.SECONDS).start();
    }
}
