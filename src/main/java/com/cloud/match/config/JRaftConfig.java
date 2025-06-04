package com.cloud.match.config;

import com.alipay.sofa.jraft.Node;
import com.alipay.sofa.jraft.entity.PeerId;
import com.alipay.sofa.jraft.option.NodeOptions;
import com.alipay.sofa.jraft.rpc.RaftRpcServerFactory;
import com.alipay.sofa.jraft.rpc.RpcServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@Slf4j
public class JRaftConfig {
    @Value("${raft.host}")
    private String host;
    @Value("${raft.port}")
    private Integer port;
    @Value("${raft.group}")
    private String group;
    @Value("${raft.serverLists}")
    private String serverLists;
    @Value("${raft.path}")
    private String path;


    private Node node;

    @PostConstruct
    void init() throws Exception {
        PeerId serverId = new PeerId(host, port);
        RpcServer server = RaftRpcServerFactory
                .createRaftRpcServer(new PeerId(host, port).getEndpoint());

        Path dir = Paths.get(path);
        if (Files.notExists(dir)) {
            Files.createDirectory(dir);
        }

        NodeOptions nodeOptions = new NodeOptions();
        // 关闭 CLI 服务
        nodeOptions.setDisableCli(true);
        // 设置选举超时时间为 1 秒
        nodeOptions.setElectionTimeoutMs(1000);
        // 每隔300秒做一次 snapshot
        nodeOptions.setSnapshotIntervalSecs(300);
        // 设置存储路径
        // 日志, 必须
        nodeOptions.setLogUri(path + File.separator + "log");
        // 元信息, 必须
        nodeOptions.setRaftMetaUri(path + File.separator + "raft_meta");
        // snapshot, 可选, 一般都推荐
        nodeOptions.setSnapshotUri(path + File.separator + "snapshot");

//        CounterStateMachine fsm = new CounterStateMachine(serializer);
//        nodeOptions.setFsm(fsm)   ;
//
//        nodeOptions.setInitialConf(JRaftUtils.getConfiguration(serverLists));
//
//        RaftGroupService groupService = new RaftGroupService(group, serverId, nodeOptions, server);
//
//        node = groupService.start();
//
//        server.registerUserProcessor(new IncrementAndGetRequestProcessor(new CounterService(node, fsm), serializer));
    }

    @PreDestroy
    void destroy() throws Exception {
        node.shutdown();
    }
}
