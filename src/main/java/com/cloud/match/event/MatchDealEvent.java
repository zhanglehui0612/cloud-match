package com.cloud.match.event;

import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
public class MatchDealEvent implements Serializable {
    @Serial
    private static final long serialVersionUID = 7502629653833921714L;

    private String tradeId;               // 成交ID
    private String symbol;              // 交易对（如：BTC/USDT）
    private String buyOrderId;            // 买单
    private String sellOrderId;           // 卖单
    private String buyerUserId;           // 买方用户ID
    private String sellerUserId;          // 卖方用户ID
    private BigDecimal price;           // 成交价格
    private BigDecimal quantity;        // 成交数量
    private long tradeTime;             // 成交时间
    private boolean isTaker;         // 是否为买方Taker
}
