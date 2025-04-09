package com.cloud.match.server;

import org.springframework.context.event.ContextRefreshedEvent;

public interface LeaderTask {

    void doWork();
}
