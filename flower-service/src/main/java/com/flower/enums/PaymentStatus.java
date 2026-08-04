package com.flower.enums;

import java.util.Locale;

public enum PaymentStatus {
    UNPAID("unpaid"),
    PAID("paid"),
    REFUNDED("refunded");

    private final String code;

    PaymentStatus(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static PaymentStatus fromCode(String value) {
        if (value == null) {
            throw new IllegalArgumentException("支付状态不能为空");
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        for (PaymentStatus status : values()) {
            if (status.code.equals(normalized)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知支付状态: " + value);
    }
}
