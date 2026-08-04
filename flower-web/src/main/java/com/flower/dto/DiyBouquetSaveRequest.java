package com.flower.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class DiyBouquetSaveRequest {
    private Long userId;

    @Size(max = 100, message = "花束名称不能超过100个字符")
    private String name;

    @NotBlank(message = "包装类型不能为空")
    @Size(max = 50, message = "包装类型不能超过50个字符")
    private String packageType;

    private BigDecimal totalPrice;

    @NotEmpty(message = "DIY花材不能为空")
    @Valid
    private List<DiyBouquetItemRequest> items;
}
