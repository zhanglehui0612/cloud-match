package com.cloud.match.server.rule;

import com.cloud.match.model.Order;
import com.cloud.match.model.OrderBook;

public class PostOnlyOrderMatchRule implements IMatchRule{
    @Override
    public String check(Order order, OrderBook orderBook) {
        // 只允许订单作为挂单进入订单簿，而不能立即成交。”
        //通常用于做市商，避免成为 taker，从而避免手续费。
        //
        //📎 举例：
        //限价买单：价格是 101，盘口卖一也是 101。
        //
        //正常来说这个单会直接撮合（成为 taker）。
        //
        //但如果是 PostOnly，则系统不允许这个单成交。
        //
        //🚨 不符合时：
        //如果订单即将撮合，会直接取消订单，而不会成交。
        return null;
    }
}
