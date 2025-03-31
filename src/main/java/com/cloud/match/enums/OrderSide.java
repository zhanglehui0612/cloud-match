package com.cloud.match.enums;

public enum OrderSide {
    BUY("BUY"),
    SELL("SELL");

    private final String side;

    OrderSide(String side) {
        this.side = side;
    }

    public String getSide() {
        return side;
    }

    @Override
    public String toString() {
        return side;
    }
}
