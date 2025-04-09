package com.cloud.match.handler;

import com.cloud.match.event.OrderEvent;
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
    public void match(MessageExt message,  OrderEvent<?> event) {
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
        this.matchEngine.doMatch(order);

        // 4 你需要考虑，撮合异常如何处理？ 发送消息失败如何处理？
    }
}
