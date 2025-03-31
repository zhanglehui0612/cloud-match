package com.cloud.match.strategies;

import com.cloud.match.model.MatchResult;
import com.cloud.match.model.Order;
import com.cloud.match.model.OrderBook;
import com.cloud.match.model.UserPosition;

public interface OrderMatchStrategy {

    MatchResult handle(Order order, OrderBook orderBook, UserPosition userPosition);
}
