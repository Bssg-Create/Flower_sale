package com.flower.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.flower.entity.DiyBouquet;
import com.flower.entity.DiyBouquetItem;
import com.flower.mapper.DiyBouquetItemMapper;
import com.flower.mapper.DiyBouquetMapper;
import com.flower.service.DiyBouquetService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DiyBouquetServiceImpl implements DiyBouquetService {
    private final DiyBouquetMapper diyBouquetMapper;
    private final DiyBouquetItemMapper diyBouquetItemMapper;

    public DiyBouquetServiceImpl(DiyBouquetMapper diyBouquetMapper, DiyBouquetItemMapper diyBouquetItemMapper) {
        this.diyBouquetMapper = diyBouquetMapper;
        this.diyBouquetItemMapper = diyBouquetItemMapper;
    }

    @Override
    @Transactional
    public DiyBouquet createBouquet(DiyBouquet bouquet, List<DiyBouquetItem> items) {
        bouquet.setCreateTime(LocalDateTime.now());
        bouquet.setUpdateTime(LocalDateTime.now());
        bouquet.setStatus("1");
        if (bouquet.getTotalPrice() == null) bouquet.setTotalPrice(BigDecimal.ZERO);
        diyBouquetMapper.insert(bouquet);
        if (items != null) {
            for (DiyBouquetItem item : items) {
                item.setBouquetId(bouquet.getId());
                diyBouquetItemMapper.insert(item);
            }
        }
        return bouquet;
    }

    @Override
    public DiyBouquet getBouquetById(Long id) { return diyBouquetMapper.selectById(id); }

    @Override
    public List<DiyBouquet> listBouquetsByUser(Long userId) {
        LambdaQueryWrapper<DiyBouquet> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DiyBouquet::getUserId, userId).orderByDesc(DiyBouquet::getCreateTime);
        return diyBouquetMapper.selectList(wrapper);
    }

    @Override
    public List<DiyBouquet> listAllBouquets() {
        LambdaQueryWrapper<DiyBouquet> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(DiyBouquet::getCreateTime);
        return diyBouquetMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public boolean deleteBouquet(Long id) {
        LambdaQueryWrapper<DiyBouquetItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(DiyBouquetItem::getBouquetId, id);
        diyBouquetItemMapper.delete(itemWrapper);
        return diyBouquetMapper.deleteById(id) > 0;
    }

    @Override
    public List<DiyBouquetItem> getBouquetItems(Long bouquetId) {
        LambdaQueryWrapper<DiyBouquetItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DiyBouquetItem::getBouquetId, bouquetId);
        return diyBouquetItemMapper.selectList(wrapper);
    }

    @Override
    public boolean updateBouquet(DiyBouquet bouquet) {
        bouquet.setUpdateTime(LocalDateTime.now());
        return diyBouquetMapper.updateById(bouquet) > 0;
    }
}