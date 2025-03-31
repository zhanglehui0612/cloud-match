package com.cloud.match.model;

import lombok.Data;
import java.io.Serial;
import java.io.Serializable;

@Data
public abstract class Snapshot implements Serializable {
    @Serial
    private static final long serialVersionUID = -6733171214841910118L;

    private long timestamp;
}
