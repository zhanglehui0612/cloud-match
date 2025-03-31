package com.cloud.match.handler;

import com.alibaba.fastjson.JSON;
import com.cloud.match.model.MatchEvent;
import com.cloud.match.model.MatchResult;
import com.cloud.match.model.Order;
import com.cloud.match.server.MatchEngine;
import com.cloud.match.service.ValidateOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;

@Slf4j
public class DealOrderMatchHandler implements MatchHandler {

    private final MatchEngine matchEngine;

    private final ValidateOrderService validateOrderService;
    public DealOrderMatchHandler(MatchEngine matchEngine, ValidateOrderService validateOrderService) {
        this.matchEngine = matchEngine;
        this.validateOrderService = validateOrderService;
    }

    @Override
    public void match(MessageExt message,  MatchEvent<?> event) {
        // 1、幂等校验
        long offset = message.getQueueOffset();
        // 之前消息已经处理过
        if (matchEngine.getOffset() >= offset) {
            log.info("消息已经处理过， offset = {}", offset);
            return;
        }

        Order order = (Order)event.getEvent();
        // 2、订单校验
        boolean result = validateOrderService.validate(order, matchEngine);
        if (!result) {
            return;
        }

        // 3、开始处理撮合
        MatchResult matchResult = this.matchEngine.doMatch(order);
        // 4 根据撮合结果，异步发送撮合结果到消息队列，包括ticker、level、depth以及撮合成交数据和取消数据.这里如果用disruptor就用disruptor， 然后再disruptor发送到对应队列中
        // 5 你需要考虑，撮合异常如何处理？ 发送消息失败如何处理？
    }
}
