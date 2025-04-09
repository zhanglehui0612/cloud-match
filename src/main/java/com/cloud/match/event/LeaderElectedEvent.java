package com.cloud.match.event;

import org.springframework.context.ApplicationEvent;

import java.io.Serial;

public class LeaderElectedEvent extends ApplicationEvent {
    @Serial
    private static final long serialVersionUID = -4124404613920129853L;

    public LeaderElectedEvent(Object source) {
        super(source);
    }
}
