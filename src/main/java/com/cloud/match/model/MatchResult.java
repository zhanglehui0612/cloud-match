package com.cloud.match.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class MatchResult implements Serializable {
    @Serial
    private static final long serialVersionUID = 5644326210061478318L;

    private String tradeId;               // 成交ID
    private String symbol;              // 交易对（如：BTC/USDT）
    private Order buyOrder;            // 买单
    private Order sellOrder;           // 卖单
    private String buyerUserId;           // 买方用户ID
    private String sellerUserId;          // 卖方用户ID
    private BigDecimal price;           // 成交价格
    private BigDecimal quantity;        // 成交数量
    private long tradeTime;             // 成交时间
    private boolean isTaker;         // 是否为买方Taker
}
