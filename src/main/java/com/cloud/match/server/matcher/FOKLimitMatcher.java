package com.cloud.match.server.matcher;

import com.cloud.match.avro.TransactionLog;
import com.cloud.match.enums.MatchEventType;
import com.cloud.match.event.MatchCancelEvent;
import com.cloud.match.event.MatchDealEvent;
import com.cloud.match.event.MatchEvent;
import com.cloud.match.model.*;
import com.cloud.match.server.rule.IMatchRule;
import com.cloud.match.store.TransactionLogStore;
import com.lmax.disruptor.dsl.Disruptor;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class FOKLimitMatcher extends LimitMatcher{
    public FOKLimitMatcher(List<IMatchRule> matchRuleList, TransactionLogStore transactionLogStore, Disruptor<MatchEvent> disruptor) {
        super(matchRuleList, transactionLogStore, disruptor);
    }

    @Override
    public void match(Order order, OrderBook orderBook, UserPosition userPosition) {
        boolean checked = preMatch(order, orderBook);
        if (!checked) {
            return;
        }

        // 检查是否当前FOK订单是否可以全部匹配
        if (matchAll(order, orderBook)) {
            // 获取订单方向,然后从买卖盘获取对手盘
            BigDecimal remainSize = order.getSize();
            do {
                String side = order.getSide();
                TreeMap<BigDecimal, TimePriorityQueue> counter = orderBook.getCounter(side);
                Map.Entry<BigDecimal, TimePriorityQueue> entry = counter.firstEntry();
                TimePriorityQueue queue = entry.getValue();
                Order countOrder = queue.peek();
                // 订单簿没有足够的对手方则退出
                if (Objects.isNull(countOrder)) {
                    break;
                }

                // 产生撮合结果
                MatchResult result = buildMatchResult(entry.getKey(), order, countOrder);
                // 构建撮合事务日志
                TransactionLog log = buildMatchLog(result);
                // 写入事务日志, 如果失败，则阻塞重试，超过重试次数则告警
                this.getTransactionLogStore().append(log);
                // 将事务日志转发到从节点

                // 更新买卖盘
                if (countOrder.getSize().compareTo(order.getSize()) == 0) {
                    orderBook.removeOrder(countOrder.getOrderId());
                } else if (countOrder.getSize().compareTo(order.getSize()) > 0) {
                    countOrder.setSize(countOrder.getSize().subtract(order.getSize()));
                }
                // 更新用户仓位
                userPosition.update(order.getUserId(), order.getSymbol(), result);
                userPosition.update(countOrder.getUserId(), countOrder.getSymbol(), result);
                // 发布撮合事件
                this.getDisruptor().publishEvent((event, seq) -> {
                    MatchDealEvent matchDealEvent = buildMatchDealEvent(result);
                    event.setMatchEventType(MatchEventType.DEAL);
                    event.setMatchDealEvent(matchDealEvent);
                });
                remainSize = remainSize.subtract(countOrder.getSize());
            } while (remainSize.compareTo(BigDecimal.ZERO) > 0);
        } else {
            // FOk 不能全部成交，则全部取消
            this.getDisruptor().publishEvent((event, seq) -> {
                MatchCancelEvent matchCancelEvent = new MatchCancelEvent(order.getOrderId(), order.getSize(), "cancel IOC order", false);
                event.setMatchEventType(MatchEventType.CANCEL);
                event.setMatchCancelEvent(matchCancelEvent);
            });
        }
    }

    private boolean matchAll(Order order, OrderBook orderBook) {
        BigDecimal totalSize = BigDecimal.ZERO;
        while (true) {
            String side = order.getSide();
            TreeMap<BigDecimal, TimePriorityQueue> counter = orderBook.getCounter(side);
            Map.Entry<BigDecimal, TimePriorityQueue> entry = counter.firstEntry();
            if (Objects.isNull(entry)) {
                return false;
            }
            TimePriorityQueue queue = entry.getValue();
            Order countOrder = queue.peek();
            if (Objects.isNull(countOrder)) {
                continue;
            }
            totalSize = totalSize.add(countOrder.getSize());
            if (totalSize.compareTo(order.getSize()) >= 0) {
                return true;
            }
        }
    }

    @Override
    public void postMatch(BigDecimal remainSize, Order order, OrderBook orderBook) {

    }
}
