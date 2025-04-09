package com.cloud.match.service.impl;

import com.cloud.match.server.MatchServer;
import com.cloud.match.server.MatchServerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class MatchServerManager {

    @Autowired
    MatchServerFactory matchServerFactory;

    private final Map<String, MatchServer> matchServerMap = new ConcurrentHashMap<>();

    public void startMatchServer(String shard) {
        if (matchServerMap.containsKey(shard)) return;
        try {
            MatchServer server = matchServerFactory.create(shard);
            server.initServer();
            server.start(shard);
            matchServerMap.put(shard, server);
        } catch (Exception e) {
            log.error("[MatchServerManager] 启动撮合symbol{} 撮合异常", shard);
        }
    }

    public void stopMatchServer(String symbol) {
        MatchServer server = matchServerMap.remove(symbol);
        if (server != null) {
            // 自定义 shutdown 逻辑
            server.shutdown();
        }
    }
}
