package com.cloud.match.server;

import com.cloud.match.config.RocketConfig;
import com.cloud.match.handler.CancelOrderMatchHandler;
import com.cloud.match.handler.DealOrderMatchHandler;
import com.cloud.match.service.IdempotentService;
import com.cloud.match.service.SnapshotService;
import com.cloud.match.service.ValidateOrderService;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class MatchServerFactory implements ApplicationContextAware {

    private ApplicationContext context;

    public MatchServer create(String symbol) {


        IdempotentService idempotentService = context.getBean(IdempotentService.class);
        SnapshotService snapshotService = context.getBean(SnapshotService.class);
        ValidateOrderService validateOrderService = context.getBean(ValidateOrderService.class);
        RocketConfig rocketConfig = context.getBean(RocketConfig.class);
        MatchEngine engine = new MatchEngine(symbol, snapshotService);

        DealOrderMatchHandler dealOrderMatchHandler = new DealOrderMatchHandler(engine, validateOrderService);
        CancelOrderMatchHandler cancelOrderMatchHandler = context.getBean(CancelOrderMatchHandler.class);
        engine.initOrderBook();
        return new MatchServer(symbol, dealOrderMatchHandler, cancelOrderMatchHandler, snapshotService, idempotentService, validateOrderService, engine, rocketConfig);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
}
