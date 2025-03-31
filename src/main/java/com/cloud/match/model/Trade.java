package com.cloud.match.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class Trade implements Serializable {
    @Serial
    private static final long serialVersionUID = -3956908787701559933L;
    private long matchId;
    private long tradeId;               // 成交ID
    private String symbol;              // 交易对（如：BTC/USDT）
    private long buyOrderId;            // 买单ID
    private long sellOrderId;           // 卖单ID
    private long buyerUserId;           // 买方用户ID
    private long sellerUserId;          // 卖方用户ID
    private BigDecimal price;           // 成交价格
    private BigDecimal quantity;        // 成交数量
    private long tradeTime;             // 成交时间
    private boolean isTaker;         // 是否为买方Taker
}
