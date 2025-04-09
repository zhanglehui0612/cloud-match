package com.cloud.match.event;

import org.springframework.context.ApplicationEvent;
import java.util.Set;
public class SymbolRemovedEvent extends ApplicationEvent {
    private final Set<String> removedSymbols;

    public SymbolRemovedEvent(Object source, Set<String> removedSymbols) {
        super(source);
        this.removedSymbols = removedSymbols;
    }

    public Set<String> getRemovedSymbols() {
        return removedSymbols;
    }
}