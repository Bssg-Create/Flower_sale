package com.flower.controller;

import com.flower.base.ResponseResult;
import com.flower.config.AuthContext;
import com.flower.entity.Order;
import com.flower.entity.OrderItem;
import com.flower.exception.BaseException;
import com.flower.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
public class OrderController {
    private final OrderService orderService;
    public OrderController(OrderService orderService) { this.orderService = orderService; }

    @PostMapping
    public ResponseResult<Order> create(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = AuthContext.getUserId(request);
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
    public ResponseResult<Order> getById(@PathVariable Long id, HttpServletRequest request) {
        return ResponseResult.success(getAuthorizedOrder(id, request));
    }

    @GetMapping("/user/{userId}")
    public ResponseResult<List<Order>> listByUser(@PathVariable Long userId, HttpServletRequest request) {
        AuthContext.requireOwnerOrAdmin(request, userId);
        return ResponseResult.success(orderService.listOrdersByUser(userId));
    }

    @GetMapping("/list")
    public ResponseResult<List<Order>> listAll(HttpServletRequest request) {
        AuthContext.requireAdmin(request);
        return ResponseResult.success(orderService.listAllOrders());
    }

    @PutMapping("/{id}/status")
    public ResponseResult<Boolean> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> params,
                                                HttpServletRequest request) {
        AuthContext.requireAdmin(request);
        return ResponseResult.success(orderService.updateOrderStatus(id, params.get("status")));
    }

    @PutMapping("/{id}/pay")
    public ResponseResult<Boolean> updatePayStatus(@PathVariable Long id, @RequestBody Map<String, String> params,
                                                   HttpServletRequest request) {
        getAuthorizedOrder(id, request);
        return ResponseResult.success(orderService.updatePayStatus(id, params.get("payStatus")));
    }

    @GetMapping("/{id}/items")
    public ResponseResult<List<OrderItem>> getItems(@PathVariable Long id, HttpServletRequest request) {
        getAuthorizedOrder(id, request);
        return ResponseResult.success(orderService.getOrderItems(id));
    }

    private Order getAuthorizedOrder(Long id, HttpServletRequest request) {
        Order order = orderService.getOrderById(id);
        if (order == null) {
            throw new BaseException(404, "订单不存在");
        }
        AuthContext.requireOwnerOrAdmin(request, order.getUserId());
        return order;
    }
}
