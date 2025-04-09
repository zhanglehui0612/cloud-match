package com.cloud.match.server.matcher;

import com.cloud.match.enums.MatchEventType;
import com.cloud.match.event.MatchCancelEvent;
import com.cloud.match.event.MatchEvent;
import com.cloud.match.model.Order;
import com.cloud.match.model.OrderBook;
import com.cloud.match.model.UserPosition;
import com.cloud.match.server.rule.IMatchRule;
import com.cloud.match.store.TransactionLogStore;
import com.lmax.disruptor.dsl.Disruptor;

import java.math.BigDecimal;
import java.util.List;

public class IOCLimitMatcher extends LimitMatcher{
    public IOCLimitMatcher(List<IMatchRule> matchRuleList, TransactionLogStore transactionLogStore, Disruptor<MatchEvent> disruptor) {
        super(matchRuleList, transactionLogStore, disruptor);
    }



    @Override
    public void postMatch(BigDecimal remainSize, Order order, OrderBook orderBook) {
        // IOC 订单无法立即成交的部分需要取消
        this.getDisruptor().publishEvent((event, seq) -> {
            MatchCancelEvent matchCancelEvent = new MatchCancelEvent(order.getOrderId(), remainSize, "cancel IOC order", true);
            event.setMatchEventType(MatchEventType.CANCEL);
            event.setMatchCancelEvent(matchCancelEvent);
        });
    }
}
