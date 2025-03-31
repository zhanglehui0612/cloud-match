package com.cloud.match.model;

import lombok.Builder;
import lombok.Data;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
public class Position implements Serializable {
    @Serial
    private static final long serialVersionUID = -5346369526016032702L;

    private String positionId;              // 仓位ID（唯一标识）
    private String userId;                  // 用户ID
    private String symbol;                // 交易对（如：BTC/USDT）
    private String orderSide;                // 持仓方向：1=多仓（BUY），-1=空仓（SELL）
    private int marginType;               // 保证金模式：0=全仓，1=逐仓
    private BigDecimal positionAmount;    // 持仓数量（正：多仓，负：空仓）
    private int positionType;             // 仓位类型：1=开仓，2=平仓
    private int positionMode;             // 持仓类型：1=净持仓，2=双向持仓
}
