package com.cloud.match.server.rule;

import com.cloud.match.model.Order;
import com.cloud.match.model.OrderBook;

public class RangePriceMatchRule implements IMatchRule{
    @Override
    public String check(Order order, OrderBook orderBook) {
        // 对“市价单”或“特殊撮合单”设置一个 最大价格范围，防止吃穿盘口，比如：
        //市价买单不能成交价格 > 当前盘口卖一价的 1.5%。
        //举例：
        //市价买单，下单时盘口卖一价为 100。
        //如果限制为 1.5%，那么最多允许匹配价格为 101.5。
        //如果对手单价格为 102，超出范围，不允许撮合。
        //不符合时：
        //直接跳过该对手单；
        //如果所有对手单都不符合范围：
        //市价单可能会部分成交 + 部分撤单。
        return null;
    }
}
