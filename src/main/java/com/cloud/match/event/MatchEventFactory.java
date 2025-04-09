package com.cloud.match.event;

import com.lmax.disruptor.EventFactory;

public class MatchEventFactory implements EventFactory<MatchEvent> {
    @Override
    public MatchEvent newInstance() {
        return new MatchEvent();
    }
}
