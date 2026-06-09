package com.flower.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("diy_bouquet")
public class DiyBouquet {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String name;
    private String packageType;
    private BigDecimal totalPrice;
    private String imageUrl;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}