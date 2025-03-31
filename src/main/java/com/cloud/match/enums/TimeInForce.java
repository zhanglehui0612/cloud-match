package com.cloud.match.enums;

public enum TimeInForce {

    GTC(1, "GTC"),   // Good 'Til Canceled
    IOC(2, "IOC"),   // Immediate Or Cancel
    FOK(3, "FOK");   // Fill Or Kill

    private final int code;  // TimeInForce 的代码
    private final String desc;  // TimeInForce 的描述

    // 构造方法
    TimeInForce(int code, String desc) {
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
    public static TimeInForce fromCode(int code) {
        for (TimeInForce timeInForce : values()) {
            if (timeInForce.getCode() == code) {
                return timeInForce;
            }
        }
        throw new IllegalArgumentException("Invalid TimeInForce code: " + code);
    }

    // 根据描述获取对应的 TimeInForce
    public static TimeInForce fromDesc(String desc) {
        for (TimeInForce timeInForce : values()) {
            if (timeInForce.getDesc().equalsIgnoreCase(desc)) {
                return timeInForce;
            }
        }
        throw new IllegalArgumentException("Invalid TimeInForce description: " + desc);
    }
}

