package com.flower.controller;

import com.flower.base.ResponseResult;
import com.flower.entity.Order;
import com.flower.entity.OrderItem;
import com.flower.service.OrderService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
public class OrderController {
    private final OrderService orderService;
    public OrderController(OrderService orderService) { this.orderService = orderService; }

    @PostMapping
    public ResponseResult<Order> create(@RequestBody Map<String, Object> params) {
        Long userId = Long.valueOf(params.get("userId").toString());
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> itemsMap = (List<Map<String, Object>>) params.get("items");
        List<OrderItem> items = itemsMap.stream().map(m -> {
            OrderItem item = new OrderItem();
            item.setFlowerId(Long.valueOf(m.get("flowerId").toString()));
            item.setQuantity(Integer.valueOf(m.get("quantity").toString()));
            return item;
        }).toList();
        return ResponseResult.success(orderService.createOrder(userId, items,
            (String) params.get("shippingAddress"), (String) params.get("receiverName"), (String) params.get("receiverPhone")));
    }

    @GetMapping("/{id}")
    public ResponseResult<Order> getById(@PathVariable Long id) { return ResponseResult.success(orderService.getOrderById(id)); }

    @GetMapping("/user/{userId}")
    public ResponseResult<List<Order>> listByUser(@PathVariable Long userId) { return ResponseResult.success(orderService.listOrdersByUser(userId)); }

    @GetMapping("/list")
    public ResponseResult<List<Order>> listAll() { return ResponseResult.success(orderService.listAllOrders()); }

    @PutMapping("/{id}/status")
    public ResponseResult<Boolean> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> params) {
        return ResponseResult.success(orderService.updateOrderStatus(id, params.get("status")));
    }

    @PutMapping("/{id}/pay")
    public ResponseResult<Boolean> updatePayStatus(@PathVariable Long id, @RequestBody Map<String, String> params) {
        return ResponseResult.success(orderService.updatePayStatus(id, params.get("payStatus")));
    }

    @GetMapping("/{id}/items")
    public ResponseResult<List<OrderItem>> getItems(@PathVariable Long id) { return ResponseResult.success(orderService.getOrderItems(id)); }
}