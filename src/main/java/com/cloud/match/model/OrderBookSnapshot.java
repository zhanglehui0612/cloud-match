package com.cloud.match.model;

import lombok.Data;

import java.io.*;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class OrderBookSnapshot extends Snapshot{
    @Serial
    private static final long serialVersionUID = -2189235187927984996L;

    private String symbol;

    private TreeMap<BigDecimal, TimePriorityQueue> bids = new TreeMap<>(Comparator.reverseOrder());

    // 卖盘：价格升序 -> 时间升序
    private TreeMap<BigDecimal, TimePriorityQueue> asks = new TreeMap<>();

    private ConcurrentHashMap<String, Order> orderIndex = new ConcurrentHashMap<>();

}
