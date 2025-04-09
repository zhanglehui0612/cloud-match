package com.cloud.match.cluster;

import com.alibaba.fastjson.JSON;
import com.cloud.match.config.ZkConfig;
import com.cloud.match.server.LeaderTask;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.leader.CancelLeadershipException;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class ZookeeperCluster implements Cluster, LeaderSelectorListener {

    public static final String LEADER_PATH = "/election";

    public static final String NODE_PATH = "/members";

    // 当前集群是否启动
    private AtomicBoolean started = new AtomicBoolean(false);

    // ZK配置
    private ZkConfig clusterConfig;

    // Zk客户端
    private CuratorFramework zkClient;

    private LeaderSelector leaderSelector;

    // 当前节点
    private final Node node;

    private PathChildrenCache pathChildrenCache;

    private LeaderTask task;

    public ZookeeperCluster(ZkConfig config, LeaderTask task) {
        this.clusterConfig = config;
        String namespace = StringUtils.defaultIfBlank(this.clusterConfig.getNamespace(), "default");
        this.zkClient = CuratorFrameworkFactory.builder()
                .connectString(this.clusterConfig.getConnectString())
                .namespace(namespace)
                .connectionTimeoutMs(this.clusterConfig.getConnectionTimeoutMs())
                .sessionTimeoutMs(this.clusterConfig.getSessionTimeoutMs())
                .retryPolicy(new ExponentialBackoffRetry(this.clusterConfig.getRetryInterval(), this.clusterConfig.getRetryTimes()))
                .build();
        this.task = task;
        this.leaderSelector = new LeaderSelector(zkClient, LEADER_PATH, this);
        this.node = Node.builder().ip(clusterConfig.getNodeIp()).port(clusterConfig.getNodePort()).leader(false).build();
        this.pathChildrenCache = new PathChildrenCache(zkClient, NODE_PATH, true);
    }


    @Override
    public void start() throws Exception {
        // 修改集群状态, 集群已经开始则忽略，因为集群做的初始化操作已经完成
        if (!started.compareAndSet(false, true)) {
            return;
        }

        // 它会触发 ZK 会话建立、连接监控线程启动、状态切换和内部异步处理线程，是整个 Curator 生命周期的起点
        this.zkClient.start();

        try {
            zkClient.create().creatingParentsIfNeeded().forPath("/members");
        } catch (Exception e) {
            log.error("[ZKCluster] create member node path error: ", e);
            throw e;
        }

        this.pathChildrenCache.getListenable().addListener((client, event) -> {
            switch (event.getType()) {
                case CHILD_ADDED:
                    log.info("[ZKCluster] node online: {}", event.getData().getPath());
                    break;
                case CHILD_REMOVED:
                    log.warn("[ZKCluster] node offline: {}", event.getData().getPath());
                    break;
                default:
                    break;
            }
        });
        this.pathChildrenCache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
    }

    @Override
    public void close() {
        if (!started.compareAndSet(true, false)) {
            return;
        }

        try {
            this.leaderSelector.close();
            this.zkClient.close();
        } catch (Exception e) {
            log.error("[ZKCluster] close error: ", e);
        }
    }

    @Override
    public void join() throws Exception {
        // 注册成员节点
        registerNodePath(node);

        // 设置Leader节点id
        leaderSelector.setId(node.getId());
        // 调用 autoRequeue() 表示你在每次 takeLeadership() 退出后自动再次参加，不需要每次手动调用 requeue()
        leaderSelector.autoRequeue();
        // 1. 保证只启动一次 2 当前不能已经是 Leader 3 注册连接状态监听器（用于重连后重新参选）
        // 4 加入选举逻辑核心 —— 创建 ZNode 并检查自己是否是 Leader
        // 当选举为Leader会触发takeLeadership

        // /election
        //   ├── leader-0000000000   // 内容是 192.168.1.101:8080
        //   ├── leader-0000000001   // 内容是 192.168.1.102:8080
        //   └── leader-0000000002   // 内容是 192.168.1.103:8080
        // 选择最小的为主节点，当主节点删除的时候，后续节点监听到之后，会接管
        this.leaderSelector.start();
    }

    private void registerNodePath(Node node) throws Exception {
        String path = NODE_PATH + node.getId();
        // 检查节点是否存在，避免重复注册
        if (zkClient.checkExists().forPath(path) != null) {
            throw new IllegalStateException("[ZKCluster] Node ID already exists: " + node.getId());
        }
        byte[] data = JSON.toJSONString(node).getBytes(StandardCharsets.UTF_8);

        zkClient.create()
                .withMode(CreateMode.EPHEMERAL)
                .forPath(path, data);
    }





    @Override
    public List<Node> members() {
        List<Node> nodes = new ArrayList<>();

        try {
            List<String> children = zkClient.getChildren().forPath("/members");
            for (String child : children) {
                String fullPath = "/members/" + child;
                byte[] data = zkClient.getData().forPath(fullPath);
                String json = new String(data, StandardCharsets.UTF_8);
                Node node = JSON.parseObject(json, Node.class);
                nodes.add(node);
            }
        } catch (Exception e) {
            log.error("[ZKCluster] get members error: ", e);
            return Collections.emptyList();
        }

        return nodes;
    }

    @Override
    public void takeLeadership(CuratorFramework curatorFramework) throws Exception {
        try {
            this.node.setLeader(true);
            updateNodeInfo();

            // 成为Leader后，开始启动撮合
            this.task.doWork();

            long nextPrint = -1;
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    TimeUnit.MILLISECONDS.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }

                long now = System.currentTimeMillis();
                if (now > nextPrint) {
                    log.info("this node is a leader");
                    nextPrint = now + 60000;
                }
            }
        } finally {
            // 主动释放 Leader 角色
            node.setLeader(false);
            updateNodeInfo();
            log.warn("this node lost leadership.");
        }
    }

    @Override
    public void stateChanged(CuratorFramework client, ConnectionState connectionState) {
        if (client.getConnectionStateErrorPolicy().isErrorState(connectionState)) {
            throw new CancelLeadershipException();
        }
    }


    private void updateNodeInfo() {
        try {
            String path = NODE_PATH + "/" + node.getId();
            byte[] data = JSON.toJSONString(node).getBytes(StandardCharsets.UTF_8);
            zkClient.setData().forPath(path, data);
        } catch (Exception e) {
            log.error("[ZKCluster] 更新节点数据失败: {}", node.getId(), e);
        }
    }

    public boolean isLeader() {
        return this.node.isLeader();
    }

    @Override
    public Node getNode() {
        return this.node;
    }
}
