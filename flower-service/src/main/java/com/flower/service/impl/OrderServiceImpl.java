package com.flower.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.flower.entity.Flower;
import com.flower.entity.Order;
import com.flower.entity.OrderItem;
import com.flower.entity.PackageType;
import com.flower.enums.OrderStatus;
import com.flower.enums.PaymentStatus;
import com.flower.exception.BaseException;
import com.flower.mapper.FlowerMapper;
import com.flower.mapper.OrderItemMapper;
import com.flower.mapper.OrderMapper;
import com.flower.service.OrderService;
import com.flower.service.PackageTypeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final FlowerMapper flowerMapper;
    private final PackageTypeService packageTypeService;

    public OrderServiceImpl(OrderMapper orderMapper, OrderItemMapper orderItemMapper, FlowerMapper flowerMapper,
                            PackageTypeService packageTypeService) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.flowerMapper = flowerMapper;
        this.packageTypeService = packageTypeService;
    }

    @Override
    @Transactional
    public Order createOrder(Long userId, List<OrderItem> items, String shippingAddress, String receiverName, String receiverPhone) {
        return createOrderInternal(userId, null, items, BigDecimal.ZERO, shippingAddress, receiverName, receiverPhone);
    }

    @Override
    @Transactional
    public Order createDiyOrder(Long userId, Long diyBouquetId, List<OrderItem> items, String packageType,
                                String shippingAddress,
                                String receiverName, String receiverPhone) {
        PackageType selectedPackage = packageTypeService.getEnabledByCompatibleName(packageType);
        BigDecimal packagePrice = requireValidPrice(selectedPackage.getPrice(), "包装价格数据异常");
        return createOrderInternal(userId, diyBouquetId, items, packagePrice, shippingAddress, receiverName,
            receiverPhone);
    }

    private Order createOrderInternal(Long userId, Long diyBouquetId, List<OrderItem> requestedItems,
                                      BigDecimal additionalAmount,
                                      String shippingAddress, String receiverName, String receiverPhone) {
        if (userId == null || userId <= 0) {
            throw new BaseException(401, "登录状态无效");
        }
        validateShippingInfo(shippingAddress, receiverName, receiverPhone);
        Map<Long, Integer> quantities = normalizeQuantities(requestedItems);
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = additionalAmount;

        for (Map.Entry<Long, Integer> entry : quantities.entrySet()) {
            Long flowerId = entry.getKey();
            Integer quantity = entry.getValue();
            Flower flower = flowerMapper.selectByIdForUpdate(flowerId);
            if (flower == null) {
                throw new BaseException(404, "花材不存在: " + flowerId);
            }
            if (!"1".equals(flower.getStatus())) {
                throw new BaseException(400, "花材已停用: " + flower.getName());
            }
            if (flower.getStock() == null || flower.getStock() < quantity) {
                throw new BaseException(409, "花材库存不足: " + flower.getName());
            }
            BigDecimal price = requireValidPrice(flower.getPrice(), "花材价格数据异常: " + flower.getName());
            if (flowerMapper.decreaseStockIfEnough(flowerId, quantity) != 1) {
                throw new BaseException(409, "花材库存发生变化，请重试: " + flower.getName());
            }

            OrderItem item = new OrderItem();
            item.setFlowerId(flowerId);
            item.setFlowerName(flower.getName());
            item.setQuantity(quantity);
            item.setPrice(price);
            item.setTotalPrice(price.multiply(BigDecimal.valueOf(quantity)));
            totalAmount = totalAmount.add(item.getTotalPrice());
            orderItems.add(item);
        }

        Order order = new Order();
        order.setOrderNo(UUID.randomUUID().toString().replace("-", "").substring(0, 20));
        order.setUserId(userId);
        order.setDiyBouquetId(diyBouquetId);
        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatus.PENDING.getCode());
        order.setPayStatus(PaymentStatus.UNPAID.getCode());
        order.setShippingAddress(shippingAddress.trim());
        order.setReceiverName(receiverName.trim());
        order.setReceiverPhone(receiverPhone.trim());
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        if (orderMapper.insert(order) != 1) {
            throw new BaseException("订单创建失败");
        }
        for (OrderItem item : orderItems) {
            item.setOrderId(order.getId());
            if (orderItemMapper.insert(item) != 1) {
                throw new BaseException("订单明细创建失败");
            }
        }
        return order;
    }

    private Map<Long, Integer> normalizeQuantities(List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            throw new BaseException(400, "订单商品不能为空");
        }
        Map<Long, Integer> quantities = new TreeMap<>();
        for (OrderItem item : items) {
            if (item == null || item.getFlowerId() == null || item.getFlowerId() <= 0) {
                throw new BaseException(400, "花材ID必须大于零");
            }
            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new BaseException(400, "商品数量必须大于零");
            }
            long merged = (long) quantities.getOrDefault(item.getFlowerId(), 0) + item.getQuantity();
            if (merged > Integer.MAX_VALUE) {
                throw new BaseException(400, "商品数量过大");
            }
            quantities.put(item.getFlowerId(), (int) merged);
        }
        return quantities;
    }

    private void validateShippingInfo(String shippingAddress, String receiverName, String receiverPhone) {
        if (isBlank(shippingAddress) || isBlank(receiverName) || isBlank(receiverPhone)) {
            throw new BaseException(400, "请填写完整的收货信息");
        }
        if (shippingAddress.trim().length() > 500 || receiverName.trim().length() > 50 ||
            receiverPhone.trim().length() > 20 || !receiverPhone.trim().matches("^[0-9+\\- ]+$")) {
            throw new BaseException(400, "收货信息格式不正确");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private BigDecimal requireValidPrice(BigDecimal price, String message) {
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new BaseException(message);
        }
        return price;
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
    @Transactional
    public boolean payOrder(Long id) {
        Order order = requireLockedOrder(id);
        OrderStatus status = parseOrderStatus(order);
        PaymentStatus payStatus = parsePaymentStatus(order);
        if (payStatus == PaymentStatus.PAID &&
            (status == OrderStatus.PAID || status == OrderStatus.SHIPPED || status == OrderStatus.COMPLETED)) {
            return true;
        }
        if (status != OrderStatus.PENDING || payStatus != PaymentStatus.UNPAID) {
            throw new BaseException(409, "当前订单状态不允许支付");
        }
        return updateState(order, OrderStatus.PAID, PaymentStatus.PAID);
    }

    @Override
    @Transactional
    public boolean cancelOrder(Long id) {
        Order order = requireLockedOrder(id);
        OrderStatus status = parseOrderStatus(order);
        PaymentStatus payStatus = parsePaymentStatus(order);
        if (status == OrderStatus.CANCELED &&
            (payStatus == PaymentStatus.UNPAID || payStatus == PaymentStatus.REFUNDED)) {
            return true;
        }
        PaymentStatus canceledPayStatus;
        if (status == OrderStatus.PENDING && payStatus == PaymentStatus.UNPAID) {
            canceledPayStatus = PaymentStatus.UNPAID;
        } else if (status == OrderStatus.PAID && payStatus == PaymentStatus.PAID) {
            canceledPayStatus = PaymentStatus.REFUNDED;
        } else {
            throw new BaseException(409, "当前订单状态不允许取消");
        }
        updateState(order, OrderStatus.CANCELED, canceledPayStatus);
        restoreStock(order.getId());
        return true;
    }

    @Override
    @Transactional
    public boolean shipOrder(Long id) {
        Order order = requireLockedOrder(id);
        OrderStatus status = parseOrderStatus(order);
        PaymentStatus payStatus = parsePaymentStatus(order);
        if (status == OrderStatus.SHIPPED && payStatus == PaymentStatus.PAID) {
            return true;
        }
        if (status != OrderStatus.PAID || payStatus != PaymentStatus.PAID) {
            throw new BaseException(409, "只有已支付订单可以发货");
        }
        return updateState(order, OrderStatus.SHIPPED, PaymentStatus.PAID);
    }

    @Override
    @Transactional
    public boolean confirmReceipt(Long id) {
        Order order = requireLockedOrder(id);
        OrderStatus status = parseOrderStatus(order);
        PaymentStatus payStatus = parsePaymentStatus(order);
        if (status == OrderStatus.COMPLETED && payStatus == PaymentStatus.PAID) {
            return true;
        }
        if (status != OrderStatus.SHIPPED || payStatus != PaymentStatus.PAID) {
            throw new BaseException(409, "只有已发货订单可以确认收货");
        }
        return updateState(order, OrderStatus.COMPLETED, PaymentStatus.PAID);
    }

    private Order requireLockedOrder(Long id) {
        Order order = id == null ? null : orderMapper.selectByIdForUpdate(id);
        if (order == null) {
            throw new BaseException(404, "订单不存在");
        }
        return order;
    }

    private OrderStatus parseOrderStatus(Order order) {
        try {
            return OrderStatus.fromCode(order.getStatus());
        } catch (IllegalArgumentException e) {
            throw new BaseException(409, "订单状态数据异常");
        }
    }

    private PaymentStatus parsePaymentStatus(Order order) {
        try {
            return PaymentStatus.fromCode(order.getPayStatus());
        } catch (IllegalArgumentException e) {
            throw new BaseException(409, "支付状态数据异常");
        }
    }

    private boolean updateState(Order order, OrderStatus status, PaymentStatus payStatus) {
        Order update = new Order();
        update.setId(order.getId());
        update.setStatus(status.getCode());
        update.setPayStatus(payStatus.getCode());
        update.setUpdateTime(LocalDateTime.now());
        if (orderMapper.updateById(update) != 1) {
            throw new BaseException(409, "订单状态已变化，请刷新后重试");
        }
        return true;
    }

    private void restoreStock(Long orderId) {
        Map<Long, Integer> quantities = new TreeMap<>();
        for (OrderItem item : getOrderItems(orderId)) {
            if (item.getFlowerId() == null || item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new BaseException(409, "订单明细数据异常，无法恢复库存");
            }
            long restored = (long) quantities.getOrDefault(item.getFlowerId(), 0) + item.getQuantity();
            if (restored > Integer.MAX_VALUE) {
                throw new BaseException(409, "订单明细数量异常，无法恢复库存");
            }
            quantities.put(item.getFlowerId(), (int) restored);
        }
        if (quantities.isEmpty()) {
            throw new BaseException(409, "订单没有商品明细，无法恢复库存");
        }
        for (Map.Entry<Long, Integer> entry : quantities.entrySet()) {
            if (flowerMapper.increaseStock(entry.getKey(), entry.getValue()) != 1) {
                throw new BaseException(409, "花材已不存在，无法恢复库存");
            }
        }
    }

    @Override
    public List<OrderItem> getOrderItems(Long orderId) {
        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getOrderId, orderId);
        return orderItemMapper.selectList(wrapper);
    }
}
