package com.cloud.match.cluster;

import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
public class Node implements Serializable {
    @Serial
    private static final long serialVersionUID = 9045800018297447249L;

    private String ip;

    private int port;

    private Long lastHeartBeatTimestamp;

    private boolean leader;

    public String getId() {
        return StringUtils.join(this.getIp(),":",this.getPort());
    }
}


