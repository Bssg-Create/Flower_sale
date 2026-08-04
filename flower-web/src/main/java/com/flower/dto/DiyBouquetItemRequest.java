package com.flower.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DiyBouquetItemRequest {
    @NotNull(message = "花材ID不能为空")
    @Positive(message = "花材ID必须大于零")
    private Long flowerId;

    @Size(max = 100, message = "花材名称不能超过100个字符")
    private String flowerName;

    @NotNull(message = "花材数量不能为空")
    @Positive(message = "花材数量必须大于零")
    private Integer quantity;

    @Size(max = 500, message = "花材位置信息不能超过500个字符")
    private String position;
}
