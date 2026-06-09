package com.flower.service;

import com.flower.entity.Order;
import com.flower.entity.OrderItem;
import java.util.List;

public interface OrderService {
    Order createOrder(Long userId, List<OrderItem> items, String shippingAddress, String receiverName, String receiverPhone);
    Order getOrderById(Long id);
    List<Order> listOrdersByUser(Long userId);
    List<Order> listAllOrders();
    boolean updateOrderStatus(Long id, String status);
    boolean updatePayStatus(Long id, String payStatus);
    List<OrderItem> getOrderItems(Long orderId);
}