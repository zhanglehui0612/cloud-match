package com.cloud.match.handler;

import com.cloud.match.enums.MatchEventType;
import com.cloud.match.event.MatchEvent;
import com.lmax.disruptor.EventHandler;

public class MatchEventHandler implements EventHandler<MatchEvent> {
    @Override
    public void onEvent(MatchEvent event, long sequence, boolean endOfBatch) throws Exception {
        MatchEventType type = event.getMatchEventType();
        if (type.getCode() == 1) {
            // 更新买卖盘
            // 1.1 增加成交数量，减少买卖盘剩余可撮合数量
            // 1.2 冰山单: 剩余可见数量(visible size = 0)才会冒出下一个visible size数量到买码盘，如果需要冒出来，则递减hidden size隐藏数量
            // 1.3 隐藏单: 如果只有隐藏单，修改隐藏单数量为剩余买卖盘订单数量
            // 1.4 Maker订单被全部撮合，从用户买卖盘订单移除(不重要，内存操作)
            // 更新最新成交价
            // 构建Level数据，推送到下游
            // 推送深度数据到下游
            // 推送成交数据到下游，比如结算
        } else if (type.getCode() == 2) {
            // 推送L2ticker数据等
            // 推送到下游，比如结算
        }
    }
}
