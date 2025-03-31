package com.cloud.match.server;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cloud.match.config.RocketConfig;
import com.cloud.match.handler.CancelOrderMatchHandler;
import com.cloud.match.handler.DealOrderMatchHandler;
import com.cloud.match.model.MatchEvent;
import com.cloud.match.model.Offset;
import com.cloud.match.model.OffsetSnapshot;
import com.cloud.match.model.Order;
import com.cloud.match.service.IdempotentService;
import com.cloud.match.service.SnapshotService;
import com.cloud.match.service.ValidateOrderService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
public class MatchServer implements Runnable {

    private ApplicationContext applicationContext;
    private final String symbol;

    private final RocketConfig rocketConfig;
    private final DealOrderMatchHandler dealOrderMatchHandler;
    private final CancelOrderMatchHandler cancelOrderMatchHandler;

    private final IdempotentService idempotentService;

    private final ValidateOrderService validateOrderService;

    private final SnapshotService snapshotService;
    private final MatchEngine matchEngine;

    private final DefaultMQPushConsumer consumer;
    private Thread recvThread;

    private Offset offset;

    public MatchServer(String symbol,
                       DealOrderMatchHandler dealOrderMatchHandler,
                       CancelOrderMatchHandler cancelOrderMatchHandler,
                       SnapshotService snapshotService,
                       IdempotentService idempotentService,
                       ValidateOrderService validateOrderService,
                       MatchEngine matchEngine,
                       RocketConfig rocketConfig) {
        this.symbol = symbol;
        this.dealOrderMatchHandler = dealOrderMatchHandler;
        this.cancelOrderMatchHandler = cancelOrderMatchHandler;
        this.snapshotService = snapshotService;
        this.idempotentService = idempotentService;
        this.validateOrderService = validateOrderService;
        this.matchEngine = matchEngine;
        this.rocketConfig = rocketConfig;


        // 从快照服务恢复offset
        OffsetSnapshot offsetSnapshot = snapshotService.loadOffsetSnapshot();
        this.offset = snapshotService.restoreOffsetFromSnapshot(offsetSnapshot);
        // 初始化MQ消费者
        try {
            this.consumer = new DefaultMQPushConsumer(StringUtils.join(this.rocketConfig.getOrderGroupPrefix() , symbol.toUpperCase()));
            consumer.setNamesrvAddr(this.rocketConfig.getNameserver());
            consumer.subscribe(StringUtils.join(this.rocketConfig.getOrderTopicPrefix(), symbol.toUpperCase()), "*");
        } catch (MQClientException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        // 1、RocketMQ开始消费数据
        try {
            consumer.registerMessageListener(new MessageListenerOrderly() {
                @Override
                public ConsumeOrderlyStatus consumeMessage(List<MessageExt> messages, ConsumeOrderlyContext context) {
                    for (MessageExt message : messages) {
                        // 2、 解析 MQ 消息（反序列化 Order 数据）
                        String msgBody = new String(message.getBody());
                        MatchEvent<?> event = parseMatchEvent(msgBody);
                        switch (event.getType()) {
                            case "DEAL":
                                dealOrderMatchHandler.match(message, event);
                            case "CANCEL":
                                cancelOrderMatchHandler.match(message, event);
                        }
                    }
                    return null;
                }
            });
            consumer.start();
        } catch (MQClientException e) {
            log.error("[MatchServer-{}] 启动 RocketMQ 消费失败，error={}", symbol, e.getMessage(), e);
        }


        // 3.1 幂等校验, 通过MQ中的offset判断现在保存的offset是否小于新订单数据在MQ中queue中offset, 需要维护一个offset在内存
        // 3.2 价格有效性校验
        // 3.3 价格范围校验
        // 3.4 平仓单用户仓位是否大于等于平仓数量校验
        // 4 将订单写事务日志WAL，写入本地磁盘
        // 5 根据订单消息类型，如果是DEAL则调用DealOrderMatchHandler处理；如果是CANCEL则调用CancelOrderMatchHandler处理
        // 6 根据撮合结果，异步发送撮合结果到消息队列，包括ticker、level、depth以及撮合成交数据和取消数据.这里如果用disruptor就用disruptor， 然后再disruptor发送到对应队列中
        // 7 你需要考虑，撮合异常如何处理？ 发送消息失败如何处理？
    }

    public void initServer() {
        matchEngine.initOrderBook();
        matchEngine.initUserPosition();
        matchEngine.initOffset();
    }

    public void start(String symbol) {
        // 创建MatchServer线程
        this.recvThread = new Thread(this, StringUtils.join("match-server-",symbol.toLowerCase(Locale.ROOT)));
        recvThread.setDaemon(true);
        // 启动线程
        recvThread.start();
    }



    private MatchEvent parseMatchEvent(String msgBody) {
        // 解析为 JSONObject 先获取 type 信息
        JSONObject jsonObject = JSON.parseObject(msgBody);

        // 获取 type 字段
        String type = jsonObject.getString("type");

        // 根据 type 动态解析不同的 event 类型
        if ("DEAL".equals(type)) {
            // 解析为 MatchEvent<Order>
            MatchEvent<Order> matchEvent = JSON.parseObject(msgBody, MatchEvent.class);
            // 手动解析 event 字段为 Order
            matchEvent.setEvent(jsonObject.getObject("event", Order.class));
            return matchEvent;
        } else if ("CANCEL".equals(type)) {
            // 解析为 MatchEvent<String>，event 是 orderId
            MatchEvent<String> matchEvent = JSON.parseObject(msgBody, MatchEvent.class);
            // 手动解析 event 字段为 String
            matchEvent.setEvent(jsonObject.getString("event"));
            return matchEvent;
        } else {
            throw new IllegalArgumentException("Unsupported type: " + type);
        }

    }

}
