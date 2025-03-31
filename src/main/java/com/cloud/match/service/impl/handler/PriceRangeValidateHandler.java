package com.cloud.match.service.impl.handler;

import com.cloud.match.exceptions.ValidationException;
import com.cloud.match.model.MatchResult;
import com.cloud.match.model.Order;
import com.cloud.match.server.MatchEngine;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PriceRangeValidateHandler implements ValidateHandler {

    private ValidateHandler next;

    @Override
    // 设置下一个处理器
    public void setNext(ValidateHandler next) {
        this.next = next;
    }

    @Override
    public void handle(Order order, MatchEngine engine) {
        if (this.next == null) {
            return;
        }

        BigDecimal price = order.getPrice();
        if (price.compareTo(BigDecimal.ZERO) <= 0 || price.compareTo(new BigDecimal("1000000")) > 0) {
            throw new ValidationException("价格不在允许的区间内");
        }

        this.next.handle(order, engine);
    }
}
