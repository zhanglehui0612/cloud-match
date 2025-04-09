package com.cloud.match.server.rule;

import com.cloud.match.model.Order;
import com.cloud.match.model.OrderBook;

public interface IMatchRule {

    String check(Order order, OrderBook orderBook);
}
