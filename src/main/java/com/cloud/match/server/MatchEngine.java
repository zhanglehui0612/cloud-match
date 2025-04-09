package com.cloud.match.server;

import com.cloud.match.enums.MatchEventType;
import com.cloud.match.event.MatchCancelEvent;
import com.cloud.match.event.MatchEvent;
import com.cloud.match.model.*;
import com.cloud.match.server.matcher.IMatcher;
import com.cloud.match.server.matcher.MatcherFactory;
import com.cloud.match.service.SnapshotService;
import com.lmax.disruptor.dsl.Disruptor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Data
public class MatchEngine {
    private String symbol;

    private OrderBook orderBook;

    private UserPosition userPosition;

    private Offset offset;

    private final SnapshotService snapshotService;

    private Disruptor<MatchEvent> disruptor;
    public MatchEngine(String symbol, Disruptor<MatchEvent> disruptor, SnapshotService snapshotService) {
        this.symbol = symbol;
        this.disruptor = disruptor;
        this.snapshotService = snapshotService;
    }


    /**
     * 初始化买卖盘
     */
    public void initOrderBook() {
        OrderBookSnapshot snapshot = this.snapshotService.loadOrderBookSnapshot(this.symbol);
        if (Objects.nonNull(snapshot)) {
            this.orderBook = this.snapshotService.restoreOrderBookFromSnapshot(snapshot);
        } else {
            this.orderBook = new OrderBook(this.symbol);
        }

    }

    public void initUserPosition() {
        UserPositionSnapshot snapshot = this.snapshotService.loadUserPositionSnapshot(this.symbol);
        if (Objects.nonNull(snapshot)) {
            this.userPosition = this.snapshotService.restoreUserPositionFromSnapshot(snapshot);
        } else {
            this.userPosition = new UserPosition(this.symbol);
        }
    }

    public void initOffset() {
        OffsetSnapshot snapshot = this.snapshotService.loadOffsetSnapshot();
        if (Objects.nonNull(snapshot)) {
            this.offset = this.snapshotService.restoreOffsetFromSnapshot(snapshot);
        } else {
            this.offset = new Offset();
        }
    }

    public void doMatch(Order order) {
        IMatcher matcher = MatcherFactory.getMatcher(order, disruptor);
        matcher.match(order, orderBook, userPosition);
    }

    public void cancelOrder(String orderId) {
        Order cancelOrder = this.orderBook.getOrderIndex().get(orderId);
        if (cancelOrder == null) {
            log.info("Can not found order {}", orderId);
            return;
        }

        // 构建取消订单事件，然后进行发布
        this.getDisruptor().publishEvent((event, seq) -> {
            MatchCancelEvent matchCancelEvent = new MatchCancelEvent(orderId, cancelOrder.getSize(), "cancel orders", false);
            event.setMatchEventType(MatchEventType.CANCEL);
            event.setMatchCancelEvent(matchCancelEvent);
        });
    }



    public Position getUserPosition(Order order) {
        Map<String, List<Position>> userPositions = this.userPosition.getUserPositions();
        if (MapUtils.isEmpty(userPositions)) {
            return null;
        }

        return this.userPosition.getUserPosition(order);
    }

    public Long getOffset() {
        Map<String, Long> offsets = this.offset.getOffsets();
        if (MapUtils.isEmpty(offsets)) {
            return null;
        }

        return offsets.get(this.symbol);
    }

    public void clear() {
        orderBook = null;
        userPosition = null;
        offset = null;
    }
}
