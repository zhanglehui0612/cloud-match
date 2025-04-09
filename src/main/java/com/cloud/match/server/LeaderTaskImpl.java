package com.cloud.match.server;

import com.cloud.match.config.CloudConfig;
import com.cloud.match.service.impl.MatchServerManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class LeaderTaskImpl implements LeaderTask {

    private final Map<String, MatchServer> symbols = new ConcurrentHashMap<>();

    CloudConfig cloudConfig;

    MatchServerManager matchServerManager;
    ContextRefreshedEvent event;


    public LeaderTaskImpl(CloudConfig cloudConfig, MatchServerManager matchServerManager, ContextRefreshedEvent event) {
        this.cloudConfig = cloudConfig;
        this.matchServerManager = matchServerManager;
        this.event = event;
    }

    @Override
    public void doWork() {
        startMatchServices(this.event);
    }


    private void startMatchServices(ContextRefreshedEvent event) {
        // 原有启动逻辑
        List<String> shards = this.cloudConfig.getSymbols();
        if (CollectionUtils.isEmpty(shards)) {
            log.error("[LeaderTaskImpl] 未分配任何交易对");
            ((ConfigurableApplicationContext) event.getApplicationContext()).close();
            return;
        }

        for (String shard : shards) {
            try {
                matchServerManager.startMatchServer(shard);
            } catch (Exception e) {
                log.error("[LeaderTaskImpl] 启动撮合symbol{} 撮合异常", shard);
            }
        }
    }
}
