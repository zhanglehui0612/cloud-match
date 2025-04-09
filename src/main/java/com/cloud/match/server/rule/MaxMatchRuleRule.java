package com.cloud.match.server.rule;

import com.cloud.match.model.Order;
import com.cloud.match.model.OrderBook;

public class MaxMatchRuleRule implements IMatchRule{
    @Override
    public String check(Order order, OrderBook orderBook) {
        // 限制一笔订单在一次撮合中最多撮合多少笔对手单。主要用于防止恶意下单攻击系统
        return null;
    }
}
