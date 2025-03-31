package com.cloud.match.handler;

import com.cloud.match.model.MatchEvent;
import com.cloud.match.server.MatchEngine;
import org.apache.rocketmq.common.message.MessageExt;

public interface MatchHandler {

    public void match(MessageExt message,  MatchEvent<?> event);
}
