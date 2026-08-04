package com.flower.controller;

import com.flower.base.ResponseResult;
import com.flower.config.AuthContext;
import com.flower.dto.OrderCreateRequest;
import com.flower.entity.Order;
import com.flower.entity.OrderItem;
import com.flower.enums.OrderStatus;
import com.flower.enums.PaymentStatus;
import com.flower.exception.BaseException;
import com.flower.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
public class OrderController {
    private final OrderService orderService;
    public OrderController(OrderService orderService) { this.orderService = orderService; }

    @PostMapping
    public ResponseResult<Order> create(@Valid @RequestBody OrderCreateRequest params, HttpServletRequest request) {
        Long userId = AuthContext.getUserId(request);
        List<OrderItem> items = params.getItems().stream().map(requestItem -> {
            OrderItem item = new OrderItem();
            item.setFlowerId(requestItem.getFlowerId());
            item.setQuantity(requestItem.getQuantity());
            return item;
        }).toList();
        return ResponseResult.success(orderService.createOrder(userId, items,
            params.getShippingAddress(), params.getReceiverName(), params.getReceiverPhone()));
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
        OrderStatus targetStatus;
        try {
            targetStatus = OrderStatus.fromCode(params.get("status"));
        } catch (IllegalArgumentException e) {
            throw new BaseException(400, e.getMessage());
        }
        if (targetStatus == OrderStatus.SHIPPED) {
            return ResponseResult.success(orderService.shipOrder(id));
        }
        if (targetStatus == OrderStatus.CANCELED) {
            return ResponseResult.success(orderService.cancelOrder(id));
        }
        throw new BaseException(409, "管理员只能执行发货或取消订单操作");
    }

    @PutMapping("/{id}/pay")
    public ResponseResult<Boolean> updatePayStatus(@PathVariable Long id, @RequestBody Map<String, String> params,
                                                   HttpServletRequest request) {
        getOwnedOrder(id, request);
        PaymentStatus targetStatus;
        try {
            targetStatus = PaymentStatus.fromCode(params.get("payStatus"));
        } catch (IllegalArgumentException e) {
            throw new BaseException(400, e.getMessage());
        }
        if (targetStatus != PaymentStatus.PAID) {
            throw new BaseException(400, "模拟支付只允许提交paid状态");
        }
        return ResponseResult.success(orderService.payOrder(id));
    }

    @PutMapping("/{id}/cancel")
    public ResponseResult<Boolean> cancel(@PathVariable Long id, HttpServletRequest request) {
        getOwnedOrder(id, request);
        return ResponseResult.success(orderService.cancelOrder(id));
    }

    @PutMapping("/{id}/confirm")
    public ResponseResult<Boolean> confirmReceipt(@PathVariable Long id, HttpServletRequest request) {
        getOwnedOrder(id, request);
        return ResponseResult.success(orderService.confirmReceipt(id));
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

    private Order getOwnedOrder(Long id, HttpServletRequest request) {
        Order order = orderService.getOrderById(id);
        if (order == null) {
            throw new BaseException(404, "订单不存在");
        }
        AuthContext.requireOwner(request, order.getUserId());
        return order;
    }
}
