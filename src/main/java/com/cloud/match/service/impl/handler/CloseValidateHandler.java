package com.cloud.match.service.impl.handler;

import com.cloud.match.exceptions.ValidationException;
import com.cloud.match.model.Order;
import com.cloud.match.model.Position;
import com.cloud.match.server.MatchEngine;
import org.springframework.stereotype.Component;

@Component
public class CloseValidateHandler implements ValidateHandler {
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

        // 处理自己核心逻辑
        Position position = engine.getUserPosition(order);
        if (position.getPositionAmount().compareTo(order.getPositionAmount()) < 0) {
            throw new ValidationException("仓位数量不足，无法平仓");
        }
        // 调用下一个责任链
        this.next.handle(order, engine);
    }
}
