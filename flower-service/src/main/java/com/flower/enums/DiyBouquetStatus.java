package com.flower.enums;

import java.util.Locale;

public enum DiyBouquetStatus {
    SAVED("saved"),
    ORDERED("ordered");

    private final String code;

    DiyBouquetStatus(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static DiyBouquetStatus fromCode(String value) {
        if (value == null) {
            throw new IllegalArgumentException("DIY状态不能为空");
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if ("1".equals(normalized)) {
            normalized = SAVED.code;
        } else if ("2".equals(normalized)) {
            normalized = ORDERED.code;
        }
        for (DiyBouquetStatus status : values()) {
            if (status.code.equals(normalized)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知DIY状态: " + value);
    }
}
