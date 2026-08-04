package com.flower.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.flower.entity.Flower;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface FlowerMapper extends BaseMapper<Flower> {
    @Select("SELECT * FROM flower WHERE id = #{id} FOR UPDATE")
    Flower selectByIdForUpdate(@Param("id") Long id);

    @Update("UPDATE flower SET stock = stock - #{quantity}, update_time = CURRENT_TIMESTAMP " +
        "WHERE id = #{id} AND status = '1' AND stock >= #{quantity}")
    int decreaseStockIfEnough(@Param("id") Long id, @Param("quantity") Integer quantity);

    @Update("UPDATE flower SET stock = stock + #{quantity}, update_time = CURRENT_TIMESTAMP WHERE id = #{id}")
    int increaseStock(@Param("id") Long id, @Param("quantity") Integer quantity);
}
