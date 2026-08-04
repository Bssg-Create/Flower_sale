package com.flower.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DiyOrderRequest {
    private Long userId;

    @NotBlank(message = "收货地址不能为空")
    @Size(max = 500, message = "收货地址不能超过500个字符")
    private String shippingAddress;

    @NotBlank(message = "收货人姓名不能为空")
    @Size(max = 50, message = "收货人姓名不能超过50个字符")
    private String receiverName;

    @NotBlank(message = "收货人电话不能为空")
    @Size(max = 20, message = "收货人电话不能超过20个字符")
    @Pattern(regexp = "^[0-9+\\- ]+$", message = "收货人电话格式不正确")
    private String receiverPhone;
}
