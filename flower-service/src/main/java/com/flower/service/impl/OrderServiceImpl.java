package com.flower.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.flower.entity.Flower;
import com.flower.entity.Order;
import com.flower.entity.OrderItem;
import com.flower.exception.BaseException;
import com.flower.mapper.FlowerMapper;
import com.flower.mapper.OrderItemMapper;
import com.flower.mapper.OrderMapper;
import com.flower.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final FlowerMapper flowerMapper;

    public OrderServiceImpl(OrderMapper orderMapper, OrderItemMapper orderItemMapper, FlowerMapper flowerMapper) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.flowerMapper = flowerMapper;
    }

    @Override
    @Transactional
    public Order createOrder(Long userId, List<OrderItem> items, String shippingAddress, String receiverName, String receiverPhone) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderItem item : items) {
            Flower flower = flowerMapper.selectById(item.getFlowerId());
            if (flower == null) throw new BaseException("花卉不存在");
            item.setPrice(flower.getPrice());
            item.setFlowerName(flower.getName());
            item.setTotalPrice(flower.getPrice().multiply(new BigDecimal(item.getQuantity())));
            totalAmount = totalAmount.add(item.getTotalPrice());
        }
        Order order = new Order();
        order.setOrderNo(UUID.randomUUID().toString().replace("-", "").substring(0, 20));
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setStatus("pending");
        order.setPayStatus("unpaid");
        order.setShippingAddress(shippingAddress);
        order.setReceiverName(receiverName);
        order.setReceiverPhone(receiverPhone);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.insert(order);
        for (OrderItem item : items) {
            item.setOrderId(order.getId());
            orderItemMapper.insert(item);
        }
        return order;
    }

    @Override
    public Order getOrderById(Long id) { return orderMapper.selectById(id); }

    @Override
    public List<Order> listOrdersByUser(Long userId) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId).orderByDesc(Order::getCreateTime);
        return orderMapper.selectList(wrapper);
    }

    @Override
    public List<Order> listAllOrders() {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Order::getCreateTime);
        return orderMapper.selectList(wrapper);
    }

    @Override
    public boolean updateOrderStatus(Long id, String status) {
        Order order = new Order();
        order.setId(id);
        order.setStatus(status);
        order.setUpdateTime(LocalDateTime.now());
        return orderMapper.updateById(order) > 0;
    }

    @Override
    public boolean updatePayStatus(Long id, String payStatus) {
        Order order = new Order();
        order.setId(id);
        order.setPayStatus(payStatus);
        order.setUpdateTime(LocalDateTime.now());
        return orderMapper.updateById(order) > 0;
    }

    @Override
    public List<OrderItem> getOrderItems(Long orderId) {
        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getOrderId, orderId);
        return orderItemMapper.selectList(wrapper);
    }
}