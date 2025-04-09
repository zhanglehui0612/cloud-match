package com.cloud.match.server;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cloud.match.config.RocketConfig;
import com.cloud.match.handler.CancelOrderMatchHandler;
import com.cloud.match.handler.DealOrderMatchHandler;
import com.cloud.match.event.OrderEvent;
import com.cloud.match.model.Offset;
import com.cloud.match.model.OffsetSnapshot;
import com.cloud.match.model.Order;
import com.cloud.match.service.IdempotentService;
import com.cloud.match.service.SnapshotService;
import com.cloud.match.service.ValidateOrderService;
import com.lmax.disruptor.dsl.Disruptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
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
                        OrderEvent<?> event = parseMatchEvent(msgBody);
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

    public void shutdown() {
        try {
            log.info("[MatchServer-{}] 开始关闭 MatchServer...", symbol);

            // 1. 关闭 MQ 消费者
            if (consumer != null) {
                consumer.shutdown();
                log.info("[MatchServer-{}] MQ 消费者已关闭", symbol);
            }

            // 2. 保存 offset 快照（确保最后一次消费位置被记录）
            OffsetSnapshot snapshot = new OffsetSnapshot();
            Map<String,Long> offsets = new HashMap<>();
            offsets.put(symbol, matchEngine.getOffset());
            snapshot.setOffsets(offsets);
            snapshot.setTimestamp(System.currentTimeMillis());
            snapshotService.saveUserOffsetSnapshot(snapshot);
            log.info("[MatchServer-{}] Offset 快照已保存", symbol);

            // 3. 中断线程（如果正在运行，可以主动中断）
            if (recvThread != null && recvThread.isAlive()) {
                recvThread.interrupt(); // 会影响 RocketMQ 消费线程吗？看实际需要
                log.info("[MatchServer-{}] 撮合线程已请求中断", symbol);
            }

            // 4. 清理本地状态（如 OrderBook、缓存等，如果需要）
            matchEngine.clear();
            log.info("[MatchServer-{}] MatchServer 已成功关闭", symbol);
        } catch (Exception e) {
            log.error("[MatchServer-{}] MatchServer 关闭失败，error={}", symbol, e.getMessage(), e);
        }
    }

    private OrderEvent parseMatchEvent(String msgBody) {
        // 解析为 JSONObject 先获取 type 信息
        JSONObject jsonObject = JSON.parseObject(msgBody);

        // 获取 type 字段
        String type = jsonObject.getString("type");

        // 根据 type 动态解析不同的 event 类型
        if ("DEAL".equals(type)) {
            // 解析为 MatchEvent<Order>
            OrderEvent<Order> orderEvent = JSON.parseObject(msgBody, OrderEvent.class);
            // 手动解析 event 字段为 Order
            orderEvent.setEvent(jsonObject.getObject("event", Order.class));
            return orderEvent;
        } else if ("CANCEL".equals(type)) {
            // 解析为 MatchEvent<String>，event 是 orderId
            OrderEvent<String> orderEvent = JSON.parseObject(msgBody, OrderEvent.class);
            // 手动解析 event 字段为 String
            orderEvent.setEvent(jsonObject.getString("event"));
            return orderEvent;
        } else {
            throw new IllegalArgumentException("Unsupported type: " + type);
        }

    }

}
