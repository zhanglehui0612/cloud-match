package com.cloud.match.enums;

public enum MatcherType {

    MARKET_MATCHER(1, "Market"),
    GTC_MATCHER(2, "GTC"),

    IOC_MATCHER(3, "IOC"),

    FOK_MATCHER(4, "FOK");

    private final int code;  // TimeInForce 的代码
    private final String desc;  // TimeInForce 的描述

    // 构造方法
    MatcherType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    // 获取 TimeInForce 的代码
    public int getCode() {
        return code;
    }

    // 获取 TimeInForce 的描述
    public String getDesc() {
        return desc;
    }

    // 根据代码获取对应的 TimeInForce
    public static MatcherType fromCode(int code) {
        for (MatcherType type : values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid matcher type code: " + code);
    }

    // 根据描述获取对应的 TimeInForce
    public static MatcherType fromDesc(String desc) {
        for (MatcherType timeInForce : values()) {
            if (timeInForce.getDesc().equalsIgnoreCase(desc)) {
                return timeInForce;
            }
        }
        throw new IllegalArgumentException("Invalid event type description: " + desc);
    }
}

