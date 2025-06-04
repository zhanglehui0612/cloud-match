package com.cloud.match.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "raft")
@Data
public class RaftProperties {
    private String groupId;          // 集群组ID
    private String nodeId;           // 当前节点ID
    private int port;                // RPC端口
    private List<String> clusterAddresses; // 集群节点列表
    private String logPath;          // 日志存储路径
    private String snapshotPath;     // 快照路径
    private int electionTimeoutMs = 3000;
    private int snapshotInterval = 3600;
}

