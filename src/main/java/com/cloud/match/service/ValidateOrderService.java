package com.cloud.match.service;

import com.cloud.match.model.Order;
import com.cloud.match.server.MatchEngine;

public interface ValidateOrderService {

    public boolean validate(Order order, MatchEngine engine);
}
