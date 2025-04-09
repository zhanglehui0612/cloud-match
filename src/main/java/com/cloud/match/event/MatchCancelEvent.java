package com.cloud.match.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MatchCancelEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = -326766199187935172L;

    private String orderId;

    private BigDecimal cancelSize;

    private String reason;

    private boolean taker;

}
