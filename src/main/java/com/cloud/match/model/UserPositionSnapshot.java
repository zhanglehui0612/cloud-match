package com.cloud.match.model;

import lombok.Data;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class UserPositionSnapshot extends Snapshot{
    @Serial
    private static final long serialVersionUID = -1730679501677661302L;

    private String symbol;

    private final Map<String, List<Position>> userPositions = new ConcurrentHashMap<>();
}
