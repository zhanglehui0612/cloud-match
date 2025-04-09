package com.cloud.match.server;

import com.cloud.match.config.RocketConfig;
import com.cloud.match.event.MatchEvent;
import com.cloud.match.event.MatchEventFactory;
import com.cloud.match.handler.CancelOrderMatchHandler;
import com.cloud.match.handler.DealOrderMatchHandler;
import com.cloud.match.event.OrderEvent;
import com.cloud.match.handler.MatchEventHandler;
import com.cloud.match.service.IdempotentService;
import com.cloud.match.service.SnapshotService;
import com.cloud.match.service.ValidateOrderService;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;

@Component
public class MatchServerFactory implements ApplicationContextAware {

    private ApplicationContext context;

    public MatchServer create(String symbol) {


        IdempotentService idempotentService = context.getBean(IdempotentService.class);
        SnapshotService snapshotService = context.getBean(SnapshotService.class);
        ValidateOrderService validateOrderService = context.getBean(ValidateOrderService.class);
        RocketConfig rocketConfig = context.getBean(RocketConfig.class);

        MatchEventFactory matchEventFactory = new MatchEventFactory();
        // 每个线程初始化独立的 Disruptor
        Disruptor<MatchEvent> disruptor = new Disruptor<>(
                matchEventFactory,
                1024,
                Executors.defaultThreadFactory(),
                ProducerType.SINGLE, // 单生产者（当前线程）
                new YieldingWaitStrategy()
        );
        disruptor.handleEventsWith(new MatchEventHandler());
        disruptor.start();

        MatchEngine engine = new MatchEngine(symbol, disruptor, snapshotService);

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
