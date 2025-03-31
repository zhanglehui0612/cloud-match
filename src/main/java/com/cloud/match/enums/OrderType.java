package com.cloud.match.enums;

public enum OrderType {

    MARKET(1, "MARKET"),
    SELL(2, "LIMIT");

    private final int code;
    private final String desc;

    OrderType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }


    // 获取订单类型的代码
    public int getCode() {
        return code;
    }

    // 获取订单类型的描述
    public String getDesc() {
        return desc;
    }

    // 根据代码获取对应的 OrderType
    public static OrderType fromCode(int code) {
        for (OrderType orderType : values()) {
            if (orderType.getCode() == code) {
                return orderType;
            }
        }
        throw new IllegalArgumentException("Invalid OrderType code: " + code);
    }

    // 根据描述获取对应的 OrderType
    public static OrderType fromDesc(String desc) {
        for (OrderType orderType : values()) {
            if (orderType.getDesc().equalsIgnoreCase(desc)) {
                return orderType;
            }
        }
        throw new IllegalArgumentException("Invalid OrderType description: " + desc);
    }
}
