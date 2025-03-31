package com.cloud.match.strategies;

import com.cloud.match.model.Order;

public class OrderMatchStrategyFactory {

    public static OrderMatchStrategy getOrderMatchStrategy(Order order) {
        OrderMatchStrategy strategy = null;
        switch (order.getOrderType()) {
            case 1:
                strategy = new MarketOrderMatchStrategy();
            case 2:
                switch (order.getTimeInForce()) {
                    case 1:
                        strategy = new GTCLimitOrderMatchStrategy();
                    case 2:
                        strategy = new IOCLimitOrderMatchStrategy();
                    case 3:
                        strategy = new FOKLimitOrderMatchStrategy();
                }
        }
        return strategy;
    }
}
