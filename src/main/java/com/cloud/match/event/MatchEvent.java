package com.cloud.match.event;

import com.cloud.match.enums.MatchEventType;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class MatchEvent implements Serializable {
    @Serial
    private static final long serialVersionUID = 7479374686879326330L;

    private MatchEventType matchEventType;

    private MatchDealEvent matchDealEvent;

    private MatchCancelEvent matchCancelEvent;
}
