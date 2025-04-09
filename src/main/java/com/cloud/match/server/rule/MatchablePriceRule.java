package com.cloud.match.server.rule;

import com.cloud.match.model.Order;
import com.cloud.match.model.OrderBook;

public class MatchablePriceRule implements IMatchRule{
    @Override
    public String check(Order order, OrderBook orderBook) {
        // 用于判断两个订单的价格是否满足撮合条件。
        // 对于限价买单：买价 ≥ 卖价。
        // 对于限价卖单：卖价 ≤ 买价。
        // 市价单：只要对手单存在就可以。
        return null;
    }
}
