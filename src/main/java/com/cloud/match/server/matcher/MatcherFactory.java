package com.cloud.match.server.matcher;


import com.cloud.match.enums.MatcherType;
import com.cloud.match.enums.OrderType;
import com.cloud.match.enums.TimeInForce;
import com.cloud.match.event.MatchEvent;
import com.cloud.match.model.Order;
import com.cloud.match.server.rule.*;
import com.cloud.match.store.TransactionLogStore;
import com.lmax.disruptor.dsl.Disruptor;
import org.apache.commons.compress.utils.Lists;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class MatcherFactory {
    private static final IMatchRule maxMatchRule = new MaxMatchRuleRule();
    private static final IMatchRule matchablePriceRule = new MatchablePriceRule();
    private static final IMatchRule rangePriceMatchRule = new RangePriceMatchRule();
    private static final IMatchRule postOnlyOrderMatchRule= new PostOnlyOrderMatchRule();
    private static final Map<MatcherType, IMatcher> matcherCache = new ConcurrentHashMap<>();
    public static IMatcher getMatcher(Order order, Disruptor<MatchEvent> disruptor) {
        MatcherType matcherType = getMatcherType(order);
        IMatcher matcher = matcherCache.get(matcherType);
        if (Objects.nonNull(matcher)) {
            return matcher;
        }


        switch (order.getOrderType()) {
            case 1:
                List<IMatchRule> marketMatchRuleList = Lists.newArrayList();
                marketMatchRuleList.add(maxMatchRule);
                marketMatchRuleList.add(rangePriceMatchRule);
                marketMatchRuleList.add(postOnlyOrderMatchRule);
                matcher = new MarketMatcher(marketMatchRuleList, new TransactionLogStore(), disruptor);
            case 2:
                List<IMatchRule> limitMatchRuleList = Lists.newArrayList();
                limitMatchRuleList.add(maxMatchRule);
                limitMatchRuleList.add(matchablePriceRule);
                limitMatchRuleList.add(rangePriceMatchRule);
                limitMatchRuleList.add(postOnlyOrderMatchRule);

                int timeInforce = order.getTimeInForce();
                if (timeInforce == 1) {
                    matcher = new GTCLimitMatcher(limitMatchRuleList, new TransactionLogStore(), disruptor);
                } else if (timeInforce == 2) {
                    matcher = new IOCLimitMatcher(limitMatchRuleList, new TransactionLogStore(), disruptor);
                } else if (timeInforce == 3) {
                    matcher = new FOKLimitMatcher(limitMatchRuleList, new TransactionLogStore(), disruptor);
                }
        }
        matcherCache.put(matcherType, matcher);
        return matcher;
    }

    private static MatcherType getMatcherType(Order order) {
        if (order.getOrderType() == OrderType.MARKET.getCode()) {
            return MatcherType.MARKET_MATCHER;
        } else if (order.getOrderType() == OrderType.LIMIT.getCode()) {
            if (order.getTimeInForce() == TimeInForce.GTC.getCode()) {
                return MatcherType.GTC_MATCHER;
            } else if (order.getTimeInForce() == TimeInForce.IOC.getCode()) {
                return MatcherType.IOC_MATCHER;
            } else if (order.getTimeInForce() == TimeInForce.FOK.getCode()) {
                return MatcherType.FOK_MATCHER;
            }
        }

        return null;
    }
}

