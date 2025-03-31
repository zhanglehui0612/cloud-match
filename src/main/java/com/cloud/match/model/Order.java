package com.cloud.match.model;


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
public class Order implements Serializable {
    @Serial
    private static final long serialVersionUID = -5794551712549484510L;
    private String orderId;
    private String userId;
    private String symbol;
    private String side;
    private BigDecimal price;
    private BigDecimal quantity;
    private int orderType;  // Limit 、 Market
    private int timeInForce; // GTC, IOC, FOK
    private boolean hidden;
    private boolean iceberg;
    private boolean postOnly;
    private int marginType;               // 保证金模式：0=全仓，1=逐仓
    private BigDecimal positionAmount;    // 持仓数量（正：多仓，负：空仓）
    private int positionType;             // 仓位类型：1=开仓，2=平仓
    private int positionMode;             // 持仓类型：1=净持仓，2=双向持仓
    private long timestamp;

}