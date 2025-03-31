package com.cloud.match.service.impl.handler;

import com.cloud.match.exceptions.ValidationException;
import com.cloud.match.model.Order;
import com.cloud.match.server.MatchEngine;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PriceValidateHandler implements ValidateHandler{
    private ValidateHandler next;

    @Override
    // 设置下一个处理器
    public void setNext(ValidateHandler next) {
        this.next = next;
    }

    @Override
    public void handle(Order order,  MatchEngine engine) {
        if (this.next == null) {
            return;
        }

        // 处理自己核心逻辑
        if (order.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("价格不允许小于0");
        }
        // 调用下一个责任链
       this.next.handle(order, engine);
    }
}
