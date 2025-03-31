package com.cloud.match.service.impl;

import com.cloud.match.constants.SnapshotConstants;
import com.cloud.match.model.*;
import com.cloud.match.service.SnapshotService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import java.io.*;
import java.math.BigDecimal;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class SnapshotServiceImpl implements SnapshotService {
    @Override
    public OrderBookSnapshot loadOrderBookSnapshot(String symbol) {
        return loadSnapshot(symbol, SnapshotConstants.ORDER_BOOK_SNAPSHOT, OrderBookSnapshot.class);
    }

    @Override
    public UserPositionSnapshot loadUserPositionSnapshot(String symbol) {
        return loadSnapshot(symbol, SnapshotConstants.USER_POSITION_SNAPSHOT, UserPositionSnapshot.class);
    }

    @Override
    public OffsetSnapshot loadOffsetSnapshot() {
        return loadSnapshot(null, SnapshotConstants.OFFSET_SNAPSHOT, OffsetSnapshot.class);
    }

    @Override
    public void saveOrderBookSnapshot(String symbol, OrderBookSnapshot snapshot) {
        saveSnapshot(symbol, SnapshotConstants.ORDER_BOOK_SNAPSHOT, snapshot);
    }

    @Override
    public void saveUserPositionSnapshot(String symbol, UserPositionSnapshot snapshot) {
        saveSnapshot(symbol, SnapshotConstants.USER_POSITION_SNAPSHOT, snapshot);
    }

    @Override
    public void saveUserOffsetSnapshot(OffsetSnapshot snapshot) {
        saveSnapshot(null, SnapshotConstants.OFFSET_SNAPSHOT, snapshot);
    }

    @Override
    public OrderBook restoreOrderBookFromSnapshot(OrderBookSnapshot snapshot) {
        TreeMap<BigDecimal, TimePriorityQueue> bids = snapshot.getBids();
        TreeMap<BigDecimal, TimePriorityQueue> asks = snapshot.getAsks();
        ConcurrentHashMap<String, Order> orderIndex = snapshot.getOrderIndex();

        OrderBook orderBook = new OrderBook(snapshot.getSymbol());
        orderBook.getBidOrders().putAll(snapshot.getBids());
        orderBook.getAskOrders().putAll(snapshot.getAsks());
        orderBook.getOrderIndex().putAll(snapshot.getOrderIndex());
        return orderBook;
    }

    @Override
    public OrderBookSnapshot createOrderBookSnapshot(OrderBook orderBook) {
        OrderBookSnapshot snapshot = new OrderBookSnapshot();
        snapshot.setSymbol(orderBook.getSymbol());
        snapshot.setBids(orderBook.getBids());
        snapshot.setAsks(orderBook.getAsks());
        snapshot.setOrderIndex(orderBook.getOrderIndex());
        snapshot.setTimestamp(System.currentTimeMillis());
        return snapshot;
    }

    @Override
    public UserPosition restoreUserPositionFromSnapshot(UserPositionSnapshot snapshot) {
        UserPosition position = new UserPosition(snapshot.getSymbol());
        position.getUserPositions().putAll(snapshot.getUserPositions());
        return position;
    }

    @Override
    public UserPositionSnapshot createUserPositionSnapshot(UserPosition userPosition) {
        UserPositionSnapshot snapshot = new UserPositionSnapshot();
        snapshot.setSymbol(userPosition.getSymbol());
        snapshot.getUserPositions().putAll(userPosition.getUserPositions());
        snapshot.setTimestamp(System.currentTimeMillis());
        return snapshot;
    }

    @Override
    public Offset restoreOffsetFromSnapshot(OffsetSnapshot snapshot) {
        Map<String,Long> offsets = snapshot.getOffsets();
        if (MapUtils.isEmpty(offsets)) {
            return null;
        }

        Offset offset = new Offset();
        offset.getOffsets().putAll(offsets);
        return offset;
    }

    @Override
    public OffsetSnapshot createOffsetSnapshot(Offset offset) {
        OffsetSnapshot snapshot = new OffsetSnapshot();
        snapshot.getOffsets().putAll(offset.getOffsets());
        snapshot.setTimestamp(System.currentTimeMillis());
        return snapshot;
    }


    /**
     * 加载快照（通用）
     */
    private <T> T loadSnapshot(String symbol, String fileName, Class<T> clazz) {
        String filePath = null;
        if (StringUtils.isNotBlank(symbol)) {
            filePath = SnapshotConstants.LOCAL_SNAPSHOT_PATH + symbol + "/" + fileName;
        } else {
            filePath = SnapshotConstants.LOCAL_SNAPSHOT_PATH + fileName;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            return clazz.cast(ois.readObject());
        } catch (IOException | ClassNotFoundException e) {
            log.error("Failed to load snapshot: {}",  filePath);
            return null;
        }
    }

    /**
     * 保存快照（通用）
     */
    private <T> void saveSnapshot(String symbol, String fileName, T snapshot) {
        String dirPath = null;
        if (StringUtils.isNotBlank(symbol)) {
            dirPath = SnapshotConstants.LOCAL_SNAPSHOT_PATH + symbol + "/";
        } else {
            dirPath = SnapshotConstants.LOCAL_SNAPSHOT_PATH;
        }
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String filePath = dirPath + fileName;
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(snapshot);
        } catch (IOException e) {
            log.error("Failed to save snapshot: {}", filePath);
        }
    }
}
