package com.flower.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.flower.entity.Flower;
import com.flower.entity.FlowerCategory;
import com.flower.mapper.FlowerCategoryMapper;
import com.flower.mapper.FlowerMapper;
import com.flower.service.FlowerService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class FlowerServiceImpl implements FlowerService {
    private final FlowerMapper flowerMapper;
    private final FlowerCategoryMapper flowerCategoryMapper;

    public FlowerServiceImpl(FlowerMapper flowerMapper, FlowerCategoryMapper flowerCategoryMapper) {
        this.flowerMapper = flowerMapper;
        this.flowerCategoryMapper = flowerCategoryMapper;
    }

    @Override
    public List<Flower> listFlowers(Long categoryId, String keyword) {
        LambdaQueryWrapper<Flower> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Flower::getStatus, "1");
        if (categoryId != null) wrapper.eq(Flower::getCategoryId, categoryId);
        if (keyword != null && !keyword.isEmpty()) wrapper.like(Flower::getName, keyword);
        wrapper.orderByDesc(Flower::getCreateTime);
        return flowerMapper.selectList(wrapper);
    }

    @Override
    public Flower getFlowerById(Long id) { return flowerMapper.selectById(id); }

    @Override
    public boolean addFlower(Flower flower) { return flowerMapper.insert(flower) > 0; }

    @Override
    public boolean updateFlower(Flower flower) { return flowerMapper.updateById(flower) > 0; }

    @Override
    public boolean deleteFlower(Long id) { return flowerMapper.deleteById(id) > 0; }

    @Override
    public List<FlowerCategory> listCategories() {
        LambdaQueryWrapper<FlowerCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FlowerCategory::getStatus, "1");
        wrapper.orderByAsc(FlowerCategory::getSortOrder);
        return flowerCategoryMapper.selectList(wrapper);
    }

    @Override
    public boolean addCategory(FlowerCategory category) { return flowerCategoryMapper.insert(category) > 0; }

    @Override
    public boolean updateCategory(FlowerCategory category) { return flowerCategoryMapper.updateById(category) > 0; }

    @Override
    public boolean deleteCategory(Long id) { return flowerCategoryMapper.deleteById(id) > 0; }
}