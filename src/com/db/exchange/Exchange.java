package com.db.exchange;

import java.util.Set;

/**
 * Created by krm on 30.09.14.
 */
public interface Exchange {
    Set<String> getAvailableSecurities();
    double getMaxBid(String security);
    double getMinAsk(String security);
    boolean buy(String security, double price);
    boolean sell(String security, double price);
}
