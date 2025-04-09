package com.cloud.match.server.matcher;


import com.cloud.match.event.MatchEvent;
import com.cloud.match.model.*;
import com.cloud.match.server.rule.IMatchRule;
import com.cloud.match.store.TransactionLogStore;
import com.lmax.disruptor.dsl.Disruptor;
import java.math.BigDecimal;
import java.util.*;

public class MarketMatcher extends BaseMatcher{

    public MarketMatcher(List<IMatchRule> matchRuleList, TransactionLogStore transactionLogStore, Disruptor<MatchEvent> disruptor) {
        super(matchRuleList, transactionLogStore, disruptor);
    }

    @Override
    public void postMatch(BigDecimal remainSize, Order order, OrderBook orderBook) {

    }


}
