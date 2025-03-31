package com.cloud.match.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class Offset implements Serializable {

    @Serial
    private static final long serialVersionUID = -3835197925516880782L;
    private Map<String, Long> offsets = new ConcurrentHashMap<>();
}
