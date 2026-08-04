package com.flower.service;

import com.flower.entity.Order;
import com.flower.entity.OrderItem;
import java.util.List;

public interface OrderService {
    Order createOrder(Long userId, List<OrderItem> items, String shippingAddress, String receiverName, String receiverPhone);
    Order createDiyOrder(Long userId, Long diyBouquetId, List<OrderItem> items, String packageType, String shippingAddress,
                         String receiverName, String receiverPhone);
    Order getOrderById(Long id);
    List<Order> listOrdersByUser(Long userId);
    List<Order> listAllOrders();
    boolean payOrder(Long id);
    boolean cancelOrder(Long id);
    boolean shipOrder(Long id);
    boolean confirmReceipt(Long id);
    List<OrderItem> getOrderItems(Long orderId);
}
