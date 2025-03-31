package com.cloud.match.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class MatchEvent<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = 7392755831116596064L;

    private String type;

    private T event;
}
