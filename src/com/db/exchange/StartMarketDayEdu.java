package com.db.exchange;

import com.db.trader.SuperHFTArbitrageBotEdu;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class StartMarketDayEdu {
    public static void main(String[] args) throws InterruptedException {
        Set<ExchangeImpl> exchanges = new HashSet<>();
        exchanges.add(new ExchangeImpl(DefaultSecurityPrices.getAllSecurities()));
        exchanges.add(new ExchangeImpl(DefaultSecurityPrices.getAllSecurities()));
        exchanges.add(new ExchangeImpl(DefaultSecurityPrices.getAllSecurities()));
        exchanges.add(new ExchangeImpl(DefaultSecurityPrices.getAllSecurities()));

        Set<MarketMaker> marketMakers = new HashSet<>();
        int i = 0;
        for (ExchangeImpl exchange : exchanges) {
            marketMakers.add(new MarketMaker(exchange, ++i));
        }
        InvisibleHandOfMarket ihm = new InvisibleHandOfMarket(exchanges);


        double balance = 0.0;
        int marketLatencyCounter = 0;
        long timeToStop = System.currentTimeMillis() + 5_000L;
        while (System.currentTimeMillis() < timeToStop) {
            for (MarketMaker mm : marketMakers) {
                mm.run();
            }
            SuperHFTArbitrageBotEdu bot = new SuperHFTArbitrageBotEdu(exchanges, 0.0, 5, TimeUnit.SECONDS);
            bot.arbitrage(exchanges);
            marketLatencyCounter++;
            if (marketLatencyCounter > 100) {
                marketLatencyCounter = 0;
                ihm.run();
            }
            balance += bot.getBalance();
        }
        System.out.println("Your balance is " + balance);
    }
}
