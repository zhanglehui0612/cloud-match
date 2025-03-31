package com.cloud.match;

import com.cloud.match.config.CloudConfig;
import com.cloud.match.server.MatchServer;
import com.cloud.match.server.MatchServerFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import java.util.List;

@Slf4j
@Component
public class ApplicationStartup implements ApplicationListener<ContextRefreshedEvent> {
    private volatile boolean started = false;

    @Autowired
    private CloudConfig cloudConfig;

    @Autowired
    MatchServerFactory matchServerFactory;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (!started) {
            started = true;
            List<String> shards = cloudConfig.getSymbols();
            if (CollectionUtils.isEmpty(shards)) {
                log.error("[ApplicationStartup] 未分配任何交易对");
                ((ConfigurableApplicationContext)event.getApplicationContext()).close();
                return;
            }

            try {
                for (String shard : shards) {
                    MatchServer matchServer = matchServerFactory.create(shard);
                    matchServer.initServer();
                    matchServer.start(shard);
                }
            } catch (Exception e) {
                log.error("[ApplicationStartup] 启动撮合服务异常，停止启动");
                ((ConfigurableApplicationContext)event.getApplicationContext()).close();
            }
        }
    }
}
