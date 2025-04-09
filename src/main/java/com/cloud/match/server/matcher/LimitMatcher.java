package com.cloud.match.server.matcher;

import com.cloud.match.event.MatchEvent;
import com.cloud.match.server.rule.IMatchRule;
import com.cloud.match.store.TransactionLogStore;
import com.lmax.disruptor.dsl.Disruptor;
import java.util.List;

public abstract class LimitMatcher extends BaseMatcher{

    public LimitMatcher(List<IMatchRule> matchRuleList, TransactionLogStore transactionLogStore, Disruptor<MatchEvent> disruptor) {
        super(matchRuleList, transactionLogStore, disruptor);
    }
}
