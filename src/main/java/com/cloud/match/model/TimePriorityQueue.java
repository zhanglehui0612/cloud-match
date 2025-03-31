package com.cloud.match.model;

import com.cloud.match.enums.OrderSide;

import java.util.Comparator;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

public class TimePriorityQueue {

    private final PriorityQueue<Order> queue = new PriorityQueue<>(Comparator.comparingLong(Order::getTimestamp));

    public void add(Order order) {
        queue.add(order);
    }

    public Order poll() {
        return queue.poll();
    }

    public Order peek() {
        return queue.peek();
    }

    public boolean remove(Order order) {
        return queue.remove(order);
    }
}
