package com.cloud.match.model;

import com.cloud.match.enums.OrderSide;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Data
public class OrderBook {
    private String symbol;
    // 买盘：价格降序 -> 时间升序
    private final TreeMap<BigDecimal, TimePriorityQueue> bids = new TreeMap<>(Comparator.reverseOrder());

    // 卖盘：价格升序 -> 时间升序
    private final TreeMap<BigDecimal, TimePriorityQueue> asks = new TreeMap<>();

    private final ConcurrentHashMap<String, Order> orderIndex = new ConcurrentHashMap<>();

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public OrderBook(String symbol) {
        this.symbol = symbol;
    }
    public void addOrder(Order order) {
        lock.writeLock().lock();
        try {
            TreeMap<BigDecimal, TimePriorityQueue> book = order.getSide() == OrderSide.BUY.getSide() ? bids : asks;

            TimePriorityQueue queue = book.computeIfAbsent(
                    order.getPrice(),
                    p -> new TimePriorityQueue()
            );

            queue.add(order);
            orderIndex.put(order.getOrderId(), order);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean removeOrder(String orderId) {
        lock.writeLock().lock();
        try {
            Order order = orderIndex.get(orderId);
            if (Objects.isNull(order)) {
                return false;
            }

            TimePriorityQueue queue = Objects.equals(order.getSide(), OrderSide.BUY.getSide()) ? bids.get(order.getPrice()) : asks.get(order.getPrice());
            if (queue == null) {
                return false;
            }
            return queue.remove(order);
        } finally {
            lock.writeLock().unlock();
        }
    }


    public TreeMap<BigDecimal, TimePriorityQueue> getCounter(String side) {
        return "BUY".equals(side) ? this.getAskOrders() : this.getBidOrders();
    }


    /**
     * 获取买一价
     * @return
     */
    public Order getBestBidPrice() {
        lock.readLock().lock();
        try {
            return bids.isEmpty() ? null : bids.firstEntry().getValue().peek();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 获取卖一价
     * @return
     */
    public Order getBestAskPrice() {
        lock.readLock().lock();
        try {
            return asks.isEmpty() ? null : asks.firstEntry().getValue().peek();
        } finally {
            lock.readLock().unlock();
        }
    }

    // 获取买单簿
    public TreeMap<BigDecimal, TimePriorityQueue> getBidOrders() {
        return bids;
    }

    // 获取卖单簿
    public TreeMap<BigDecimal, TimePriorityQueue> getAskOrders() {
        return asks;
    }

}
