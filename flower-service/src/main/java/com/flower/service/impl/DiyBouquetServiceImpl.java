package com.flower.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.flower.entity.DiyBouquet;
import com.flower.entity.DiyBouquetItem;
import com.flower.entity.Flower;
import com.flower.entity.Order;
import com.flower.entity.OrderItem;
import com.flower.entity.PackageType;
import com.flower.enums.DiyBouquetStatus;
import com.flower.exception.BaseException;
import com.flower.mapper.DiyBouquetItemMapper;
import com.flower.mapper.DiyBouquetMapper;
import com.flower.mapper.FlowerMapper;
import com.flower.service.DiyBouquetService;
import com.flower.service.OrderService;
import com.flower.service.PackageTypeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class DiyBouquetServiceImpl implements DiyBouquetService {
    private final DiyBouquetMapper diyBouquetMapper;
    private final DiyBouquetItemMapper diyBouquetItemMapper;
    private final FlowerMapper flowerMapper;
    private final PackageTypeService packageTypeService;
    private final OrderService orderService;

    public DiyBouquetServiceImpl(DiyBouquetMapper diyBouquetMapper, DiyBouquetItemMapper diyBouquetItemMapper,
                                 FlowerMapper flowerMapper, PackageTypeService packageTypeService,
                                 OrderService orderService) {
        this.diyBouquetMapper = diyBouquetMapper;
        this.diyBouquetItemMapper = diyBouquetItemMapper;
        this.flowerMapper = flowerMapper;
        this.packageTypeService = packageTypeService;
        this.orderService = orderService;
    }

    @Override
    @Transactional
    public DiyBouquet createBouquet(DiyBouquet bouquet, List<DiyBouquetItem> items) {
        if (bouquet == null || bouquet.getUserId() == null || bouquet.getUserId() <= 0) {
            throw new BaseException(401, "登录状态无效");
        }
        if (items == null || items.isEmpty()) {
            throw new BaseException(400, "DIY花材不能为空");
        }
        String name = bouquet.getName() == null ? "" : bouquet.getName().trim();
        bouquet.setName(name.isEmpty() ? "我的花束" : name);
        PackageType selectedPackage = packageTypeService.getEnabledByCompatibleName(bouquet.getPackageType());
        BigDecimal packagePrice = requireValidPrice(selectedPackage.getPrice(), "包装价格数据异常");
        BigDecimal totalPrice = packagePrice;
        Map<Long, Flower> flowers = new HashMap<>();

        for (DiyBouquetItem item : items) {
            validateItem(item);
            Flower flower = flowers.computeIfAbsent(item.getFlowerId(), flowerMapper::selectById);
            if (flower == null) {
                throw new BaseException(404, "花材不存在: " + item.getFlowerId());
            }
            if (!"1".equals(flower.getStatus())) {
                throw new BaseException(400, "花材已停用: " + flower.getName());
            }
            BigDecimal price = requireValidPrice(flower.getPrice(), "花材价格数据异常: " + flower.getName());
            item.setFlowerName(flower.getName());
            totalPrice = totalPrice.add(price.multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        bouquet.setPackageType(selectedPackage.getName());
        bouquet.setTotalPrice(totalPrice);
        bouquet.setCreateTime(LocalDateTime.now());
        bouquet.setUpdateTime(LocalDateTime.now());
        bouquet.setStatus(DiyBouquetStatus.SAVED.getCode());
        if (diyBouquetMapper.insert(bouquet) != 1) {
            throw new BaseException("DIY花束保存失败");
        }
        for (DiyBouquetItem item : items) {
            item.setBouquetId(bouquet.getId());
            if (diyBouquetItemMapper.insert(item) != 1) {
                throw new BaseException("DIY花材保存失败");
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
        DiyBouquet bouquet = diyBouquetMapper.selectById(id);
        if (bouquet == null) {
            throw new BaseException(404, "花束方案不存在");
        }
        DiyBouquetStatus status = parseStatus(bouquet);
        if (status == DiyBouquetStatus.ORDERED) {
            throw new BaseException(409, "已下单的花束方案不能删除");
        }
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

    @Override
    @Transactional
    public Order placeOrder(Long bouquetId, Long userId, String shippingAddress, String receiverName,
                            String receiverPhone) {
        DiyBouquet bouquet = diyBouquetMapper.selectById(bouquetId);
        if (bouquet == null) {
            throw new BaseException(404, "花束方案不存在");
        }
        if (!Objects.equals(userId, bouquet.getUserId())) {
            throw new BaseException(403, "无权操作他人的花束方案");
        }
        DiyBouquetStatus status = parseStatus(bouquet);
        if (status == DiyBouquetStatus.ORDERED) {
            throw new BaseException(409, "花束方案已下单");
        }
        if (status != DiyBouquetStatus.SAVED) {
            throw new BaseException(409, "当前花束方案状态不允许下单");
        }

        LocalDateTime now = LocalDateTime.now();
        LambdaUpdateWrapper<DiyBouquet> claimWrapper = new LambdaUpdateWrapper<>();
        claimWrapper.eq(DiyBouquet::getId, bouquetId)
            .eq(DiyBouquet::getUserId, userId)
            .in(DiyBouquet::getStatus, DiyBouquetStatus.SAVED.getCode(), "1")
            .set(DiyBouquet::getStatus, DiyBouquetStatus.ORDERED.getCode())
            .set(DiyBouquet::getUpdateTime, now);
        if (diyBouquetMapper.update(null, claimWrapper) != 1) {
            throw new BaseException(409, "花束方案状态已变化，请刷新后重试");
        }

        List<DiyBouquetItem> diyItems = getBouquetItems(bouquetId);
        if (diyItems.isEmpty()) {
            throw new BaseException(400, "花束方案没有花材");
        }
        List<OrderItem> orderItems = diyItems.stream().map(diyItem -> {
            OrderItem orderItem = new OrderItem();
            orderItem.setFlowerId(diyItem.getFlowerId());
            orderItem.setQuantity(diyItem.getQuantity());
            return orderItem;
        }).toList();

        Order order = orderService.createDiyOrder(userId, bouquetId, orderItems, bouquet.getPackageType(),
            shippingAddress, receiverName, receiverPhone);
        DiyBouquet priceUpdate = new DiyBouquet();
        priceUpdate.setId(bouquetId);
        priceUpdate.setTotalPrice(order.getTotalAmount());
        priceUpdate.setUpdateTime(LocalDateTime.now());
        if (diyBouquetMapper.updateById(priceUpdate) != 1) {
            throw new BaseException("花束方案状态更新失败");
        }
        return order;
    }

    private void validateItem(DiyBouquetItem item) {
        if (item == null || item.getFlowerId() == null || item.getFlowerId() <= 0) {
            throw new BaseException(400, "花材ID必须大于零");
        }
        if (item.getQuantity() == null || item.getQuantity() <= 0) {
            throw new BaseException(400, "花材数量必须大于零");
        }
        if (item.getPosition() != null && item.getPosition().length() > 500) {
            throw new BaseException(400, "花材位置信息不能超过500个字符");
        }
    }

    private BigDecimal requireValidPrice(BigDecimal price, String message) {
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new BaseException(message);
        }
        return price;
    }

    private DiyBouquetStatus parseStatus(DiyBouquet bouquet) {
        try {
            return DiyBouquetStatus.fromCode(bouquet.getStatus());
        } catch (IllegalArgumentException e) {
            throw new BaseException(409, "DIY花束状态数据异常");
        }
    }
}
