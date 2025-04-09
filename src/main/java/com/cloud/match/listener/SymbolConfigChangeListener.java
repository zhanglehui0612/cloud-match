package com.cloud.match.listener;

import com.alibaba.nacos.api.config.annotation.NacosConfigListener;
import com.cloud.match.event.SymbolAddEvent;
import com.cloud.match.event.SymbolRemovedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class SymbolConfigChangeListener {

    private final ApplicationEventPublisher publisher;

    private Set<String> currentSymbols = new HashSet<>();

    public SymbolConfigChangeListener(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @NacosConfigListener(dataId = "application.properties", groupId = "DEFAULT_GROUP")
    public void onConfigChanged(String config) {
        String newSymbolsStr = getValueFromConfig(config, "symbols");
        if (newSymbolsStr == null) return;

        Set<String> newSymbols = Arrays.stream(newSymbolsStr.split(","))
                .map(String::trim)
                .collect(Collectors.toSet());

        Set<String> added = new HashSet<>(newSymbols);
        added.removeAll(currentSymbols);

        Set<String> removed = new HashSet<>(currentSymbols);
        removed.removeAll(newSymbols);

        if (!added.isEmpty()) {
            log.info("新增交易对：{}", added);
            publisher.publishEvent(new SymbolAddEvent(this, added));
        }

        if (!removed.isEmpty()) {
            log.info("移除交易对：{}", removed);
            publisher.publishEvent(new SymbolRemovedEvent(this, removed));
        }

        this.currentSymbols = newSymbols;
    }

    private String getValueFromConfig(String config, String key) {
        Properties props = new Properties();
        try {
            props.load(new StringReader(config));
            return props.getProperty(key);
        } catch (IOException e) {
            log.error("解析 Nacos 配置出错", e);
            return null;
        }
    }
}
