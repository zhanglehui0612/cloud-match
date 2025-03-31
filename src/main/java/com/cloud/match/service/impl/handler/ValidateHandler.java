package com.cloud.match.service.impl.handler;

import com.cloud.match.model.Offset;
import com.cloud.match.model.Order;
import com.cloud.match.server.MatchEngine;

public interface ValidateHandler {

    public void handle(Order order, MatchEngine engine);

    public void setNext(ValidateHandler handler);
}
