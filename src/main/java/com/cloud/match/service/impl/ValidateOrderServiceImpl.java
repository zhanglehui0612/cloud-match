package com.cloud.match.service.impl;

import com.cloud.match.model.Order;
import com.cloud.match.server.MatchEngine;
import com.cloud.match.service.ValidateOrderService;
import com.cloud.match.service.impl.handler.ValidateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ValidateOrderServiceImpl implements ValidateOrderService {

    @Autowired
    private List<ValidateHandler> validateHandlers;


    @Override
    public boolean validate(Order order, MatchEngine engine) {
        // 构建责任链
        ValidateHandler head = buildHandlerChain(validateHandlers);

        try {
            // 执行责任链
            head.handle(order, engine);
            return true;  // 校验通过
        } catch (Exception e) {
            // 处理校验失败的情况
            log.error("订单校验失败：{}", e.getMessage());
            return false;
        }
    }



    // 构建责任链，按顺序链接所有处理器
    private ValidateHandler buildHandlerChain(List<ValidateHandler> handlers) {
        if (handlers.isEmpty()) {
            throw new IllegalStateException("未配置任何校验处理器");
        }

        ValidateHandler head = handlers.get(0);
        ValidateHandler current = head;

        for (int i = 1; i < handlers.size(); i++) {
            current.setNext(handlers.get(i));
        }
        return head;
    }
}
