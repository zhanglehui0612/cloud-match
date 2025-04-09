package com.cloud.match.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "zookeeper")
public class ZkConfig {
    private String connectString = "localhost:2181";
    private String namespace = "match-cluster";
    private int sessionTimeoutMs = 60000;
    private int connectionTimeoutMs = 15000;
    private int retryInterval;
    private int retryTimes;
    private String nodeIp;
    private int nodePort;

}