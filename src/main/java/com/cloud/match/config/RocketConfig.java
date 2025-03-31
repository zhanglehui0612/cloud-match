package com.cloud.match.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class RocketConfig {

    @Value("${cloud.match.order.rocketmq.nameserver}")
    private String nameserver;

    @Value("${cloud.match.order.rocketmq.orderGroupPrefix}")
    private String orderGroupPrefix;

    @Value("${cloud.match.order.rocketmq.orderTopicPrefix}")
    private String orderTopicPrefix;
}
