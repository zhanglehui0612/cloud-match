package com.cloud.match.server.matcher;

import com.cloud.match.event.MatchEvent;
import com.cloud.match.model.Order;
import com.cloud.match.model.OrderBook;
import com.cloud.match.server.rule.IMatchRule;
import com.cloud.match.store.TransactionLogStore;
import com.lmax.disruptor.dsl.Disruptor;

import java.math.BigDecimal;
import java.util.List;

public class GTCLimitMatcher extends LimitMatcher{
    public GTCLimitMatcher(List<IMatchRule> matchRuleList, TransactionLogStore transactionLogStore, Disruptor<MatchEvent> disruptor) {
        super(matchRuleList, transactionLogStore, disruptor);
    }

    @Override
    public void postMatch(BigDecimal remainSize, Order order, OrderBook orderBook) {
        // GTC 剩余无法匹配的订单需要挂到买卖盘
        order.setSize(remainSize);
        orderBook.addOrder(order);
    }
}
