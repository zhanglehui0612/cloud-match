package com.cloud.match.enums;

public enum MatchEventType {

    DEAL(1, "DEAL"),
    CANCEL(2, "CANCEL");


    private final int code;  // TimeInForce 的代码
    private final String desc;  // TimeInForce 的描述

    // 构造方法
    MatchEventType(int code, String desc) {
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
    public static MatchEventType fromCode(int code) {
        for (MatchEventType type : values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid event type code: " + code);
    }

    // 根据描述获取对应的 TimeInForce
    public static MatchEventType fromDesc(String desc) {
        for (MatchEventType timeInForce : values()) {
            if (timeInForce.getDesc().equalsIgnoreCase(desc)) {
                return timeInForce;
            }
        }
        throw new IllegalArgumentException("Invalid event type description: " + desc);
    }
}

