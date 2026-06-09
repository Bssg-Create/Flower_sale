package com.flower.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("diy_bouquet_item")
public class DiyBouquetItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long bouquetId;
    private Long flowerId;
    private String flowerName;
    private Integer quantity;
    private String position;
}