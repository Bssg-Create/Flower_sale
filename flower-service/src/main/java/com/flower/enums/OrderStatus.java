package com.flower.enums;

import java.util.Locale;

public enum OrderStatus {
    PENDING("pending"),
    PAID("paid"),
    SHIPPED("shipped"),
    COMPLETED("completed"),
    CANCELED("canceled");

    private final String code;

    OrderStatus(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static OrderStatus fromCode(String value) {
        if (value == null) {
            throw new IllegalArgumentException("订单状态不能为空");
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if ("payed".equals(normalized)) {
            normalized = PAID.code;
        } else if ("cancelled".equals(normalized)) {
            normalized = CANCELED.code;
        }
        for (OrderStatus status : values()) {
            if (status.code.equals(normalized)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知订单状态: " + value);
    }
}
