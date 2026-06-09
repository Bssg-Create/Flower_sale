package com.flower.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.flower.entity.Flower;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FlowerMapper extends BaseMapper<Flower> {
}