package com.flower.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class OrderItemRequest {
    @NotNull(message = "花材ID不能为空")
    @Positive(message = "花材ID必须大于零")
    private Long flowerId;

    @NotNull(message = "商品数量不能为空")
    @Positive(message = "商品数量必须大于零")
    private Integer quantity;
}
