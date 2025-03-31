package com.cloud.match.service;

import com.cloud.match.model.*;
import org.apache.commons.collections.MapUtils;

import java.math.BigDecimal;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

public interface SnapshotService {

    public OrderBookSnapshot loadOrderBookSnapshot(String symbol);

    public UserPositionSnapshot loadUserPositionSnapshot(String symbol);

    public OffsetSnapshot loadOffsetSnapshot();

    public void saveOrderBookSnapshot(String symbol, OrderBookSnapshot snapshot);

    public void saveUserPositionSnapshot(String symbol, UserPositionSnapshot snapshot);

    public void saveUserOffsetSnapshot(OffsetSnapshot snapshot);


    public OrderBook restoreOrderBookFromSnapshot(OrderBookSnapshot snapshot);

    public OrderBookSnapshot createOrderBookSnapshot(OrderBook orderBook);

    public UserPosition restoreUserPositionFromSnapshot(UserPositionSnapshot snapshot);

    public UserPositionSnapshot createUserPositionSnapshot(UserPosition userPosition);

    public Offset restoreOffsetFromSnapshot(OffsetSnapshot snapshot);


    public OffsetSnapshot createOffsetSnapshot(Offset offset);

}
