package com.cloud.match;

import com.cloud.match.cluster.ZookeeperCluster;
import com.cloud.match.config.CloudConfig;
import com.cloud.match.config.ZkConfig;
import com.cloud.match.server.LeaderTask;
import com.cloud.match.server.LeaderTaskImpl;
import com.cloud.match.service.impl.MatchServerManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class ApplicationStartup implements ApplicationListener<ContextRefreshedEvent> {
    private volatile boolean started = false;

    @Autowired
    private ZkConfig zkConfig;

    @Autowired
    CloudConfig cloudConfig;

    @Autowired
    MatchServerManager matchServerManager;

    private ZookeeperCluster cluster;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        try {
            LeaderTask task = new LeaderTaskImpl(cloudConfig, matchServerManager, event);
            this.cluster = new ZookeeperCluster(zkConfig, task);
            if (!started) {
                started = true;
                log.info("当前节点成为Leader，开始加入集群");
                this.cluster.join();
            }
        } catch (Exception e) {
            log.error("[ApplicationStartup] 撮合启动失败，节点ID: {}", cluster.getNode().getIp());
        } finally {
            started = false;
        }
    }
}
