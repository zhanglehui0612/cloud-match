package com.cloud.match.server.matcher;

import com.cloud.match.event.MatchEvent;
import com.cloud.match.event.OrderEvent;
import com.cloud.match.model.*;
import com.cloud.match.server.rule.IMatchRule;
import com.lmax.disruptor.dsl.Disruptor;

import java.util.List;

public interface IMatcher {
    List<IMatchRule> getMatchRuleList();
    void match(Order order, OrderBook orderBook, UserPosition userPosition);
}
