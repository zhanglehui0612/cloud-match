package com.cloud.match.server.matcher;

import com.alibaba.fastjson.JSON;
import com.cloud.match.avro.TransactionLog;
import com.cloud.match.enums.MatchEventType;
import com.cloud.match.event.MatchCancelEvent;
import com.cloud.match.event.MatchDealEvent;
import com.cloud.match.event.MatchEvent;
import com.cloud.match.model.*;
import com.cloud.match.server.rule.IMatchRule;
import com.cloud.match.store.TransactionLogStore;
import com.lmax.disruptor.dsl.Disruptor;
import lombok.Getter;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import java.math.BigDecimal;
import java.util.*;

public abstract class BaseMatcher implements IMatcher{
    @Getter
    private TransactionLogStore transactionLogStore;

    private List<IMatchRule> matchRuleList;

    @Getter
    private Disruptor<MatchEvent> disruptor;

    public BaseMatcher(List<IMatchRule> matchRuleList, TransactionLogStore transactionLogStore, Disruptor<MatchEvent> disruptor) {
        this.matchRuleList = matchRuleList;
        this.transactionLogStore = transactionLogStore;
        this.disruptor = disruptor;
    }

    @Override
    public List<IMatchRule> getMatchRuleList() {
        return this.matchRuleList;
    }

    @Override
    public void match(Order order, OrderBook orderBook, UserPosition userPosition) {
        boolean checked = preMatch(order, orderBook);
        if (!checked) {
            return;
        }

        // 获取订单方向,然后从买卖盘获取对手盘
        BigDecimal remainSize = order.getSize();
        do {
            TimePriorityQueue queue  = findBestPriceQueue(order.getPrice(), order.getSide(), orderBook);
            // 订单簿没有足够的对手方则退出
            if (queue == null) {
                break;
            }


            Order countOrder = queue.peek();
            // 当前价格队列中没有订单，则从下一个价格队列获取订单
            if (Objects.isNull(countOrder)) {
                continue;
            }

            // 产生撮合结果
            MatchResult result = buildMatchResult(countOrder.getPrice(), order, countOrder);
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

        postMatch(remainSize, order, orderBook);
    }

    public boolean preMatch(Order order, OrderBook orderBook) {
        List<IMatchRule> matchRuleList = this.getMatchRuleList();
        for (IMatchRule rule : matchRuleList) {
            String reason = rule.check(order, orderBook);
            if (!StringUtils.isBlank(reason)) {
                // 构建取消订单事件，然后进行发布
                this.getDisruptor().publishEvent((event, seq) -> {
                    MatchCancelEvent matchCancelEvent = new MatchCancelEvent(order.getOrderId(), order.getSize(), reason, false);
                    event.setMatchEventType(MatchEventType.CANCEL);
                    event.setMatchCancelEvent(matchCancelEvent);
                });
                // 结束撮合流程
                return Boolean.FALSE;
            }
        }

        return Boolean.TRUE;
    }


    public abstract void postMatch(BigDecimal remainSize, Order order, OrderBook orderBook);

    MatchResult buildMatchResult(BigDecimal tradePrice, Order order, Order countOrder) {
        MatchResult result = new MatchResult();
        result.setTradeId(UUID.randomUUID().toString());
        result.setPrice(tradePrice);
        result.setSymbol(order.getSymbol());
        result.setQuantity(order.getSize());
        if (order.getSide().equals("BUY")) {
            result.setBuyerUserId(order.getUserId());
            result.setBuyOrderId(order.getOrderId());
            result.setSellerUserId(countOrder.getUserId());
            result.setSellOrderId(countOrder.getOrderId());
            result.setBuyerOrder(order);
            Order copyCounterOrder = new Order();
            BeanUtils.copyProperties(countOrder, copyCounterOrder);
            copyCounterOrder.setSize(copyCounterOrder.getSize().subtract(order.getSize()));
            result.setSellerOrder(copyCounterOrder);
        } else {
            result.setBuyerUserId(countOrder.getUserId());
            result.setBuyOrderId(countOrder.getOrderId());
            result.setSellerUserId(order.getUserId());
            result.setSellOrderId(order.getOrderId());
            Order copyCounterOrder = new Order();
            BeanUtils.copyProperties(countOrder, copyCounterOrder);
            copyCounterOrder.setSize(copyCounterOrder.getSize().subtract(order.getSize()));
            result.setBuyerOrder(copyCounterOrder);
            result.setSellerOrder(order);
        }

        return result;
    }


    private TimePriorityQueue findBestPriceQueue(BigDecimal price, String side, OrderBook orderBook) {
        TreeMap<BigDecimal, TimePriorityQueue> counter = orderBook.getCounter(side);
        TimePriorityQueue queue = counter.get(price);
        if (queue != null) {
            return queue;
        }

        if ("BUY".equals(side)) {
            SortedMap<BigDecimal, TimePriorityQueue> asks =  counter.headMap(price, false).descendingMap();
            return MapUtils.isEmpty(asks) ? null : asks.entrySet().iterator().next().getValue();
        }

        if ("SELL".equals(side)) {
            SortedMap<BigDecimal, TimePriorityQueue> bids =  counter.headMap(price, false);
            return MapUtils.isEmpty(bids) ? null : bids.entrySet().iterator().next().getValue();
        }

        return null;
    }


    TransactionLog buildMatchLog(MatchResult result) {
        TransactionLog.Builder builder = TransactionLog.newBuilder();

        builder.setId(System.currentTimeMillis()+ RandomUtils.nextInt(100000,999999));
        builder.setSymbol(result.getSymbol());
        builder.setType("DEAL");
        builder.setTimestamp(result.getTradeTime());
        builder.setData(JSON.toJSONString(result));
        return builder.build();
    }


    MatchDealEvent buildMatchDealEvent(MatchResult result) {
        return MatchDealEvent.builder()
                .tradeId(result.getTradeId())
                .price(result.getPrice())
                .quantity(result.getQuantity())
                .tradeTime(result.getTradeTime())
                .sellOrderId(result.getSellOrderId())
                .sellerUserId(result.getSellerUserId())
                .buyerUserId(result.getBuyerUserId())
                .buyOrderId(result.getBuyOrderId())
                .build();
    }
}
