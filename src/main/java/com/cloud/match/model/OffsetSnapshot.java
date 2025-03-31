package com.cloud.match.model;

import lombok.Data;

import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class OffsetSnapshot extends Snapshot{
    @Serial
    private static final long serialVersionUID = -575304683055789646L;

    private Map<String, Long> offsets = new ConcurrentHashMap<>();
}
