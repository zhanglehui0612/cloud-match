package com.cloud.match.event;

import org.springframework.context.ApplicationEvent;

import java.util.Set;

public class SymbolAddEvent extends ApplicationEvent {
    private final Set<String> addedSymbols;

    public SymbolAddEvent(Object source, Set<String> addedSymbols) {
        super(source);
        this.addedSymbols = addedSymbols;
    }

    public Set<String> getAddedSymbols() {
        return addedSymbols;
    }
}