package com.cloud.match.handler;

import com.cloud.match.event.OrderEvent;
import org.apache.rocketmq.common.message.MessageExt;

public interface MatchHandler {

    public void match(MessageExt message,  OrderEvent<?> event);
}
