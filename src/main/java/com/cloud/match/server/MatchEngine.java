package com.cloud.match.server;

import com.cloud.match.model.*;
import com.cloud.match.service.PositionManager;
import com.cloud.match.service.SnapshotService;
import com.cloud.match.strategies.OrderMatchStrategy;
import com.cloud.match.strategies.OrderMatchStrategyFactory;
import lombok.Data;
import org.apache.commons.collections.MapUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Data
public class MatchEngine {
    private String symbol;

    private OrderBook orderBook;

    private UserPosition userPosition;

    private Offset offset;

    private final SnapshotService snapshotService;

    public MatchEngine(String symbol, SnapshotService snapshotService) {
        this.symbol = symbol;
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

    public MatchResult doMatch(Order order) {
        OrderMatchStrategy strategy = OrderMatchStrategyFactory.getOrderMatchStrategy(order);
        return strategy.handle(order, orderBook, userPosition);
    }

    public MatchResult cancelOrder(String orderId) {
        return null;
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
}
