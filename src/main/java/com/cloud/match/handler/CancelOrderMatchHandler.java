package com.cloud.match.handler;

import com.cloud.match.event.OrderEvent;
import com.cloud.match.server.MatchEngine;
import org.apache.rocketmq.common.message.MessageExt;

public class CancelOrderMatchHandler implements MatchHandler {
    private final MatchEngine matchEngine;

    public CancelOrderMatchHandler(MatchEngine matchEngine) {
        this.matchEngine = matchEngine;
    }

    @Override
    public void match(MessageExt message, OrderEvent<?> event) {
        String orderId = (String) event.getEvent();
        this.matchEngine.cancelOrder(orderId);
    }
}
