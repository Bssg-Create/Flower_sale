package com.flower.controller;

import com.flower.base.ResponseResult;
import com.flower.config.AuthContext;
import com.flower.entity.DiyBouquet;
import com.flower.entity.DiyBouquetItem;
import com.flower.entity.Flower;
import com.flower.entity.Order;
import com.flower.entity.OrderItem;
import com.flower.entity.PackageType;
import com.flower.exception.BaseException;
import com.flower.service.DiyBouquetService;
import com.flower.service.FlowerService;
import com.flower.service.OrderService;
import com.flower.service.PackageTypeService;
import lombok.extern.slf4j.Slf4j;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/diy")
public class DiyController {

    private final DiyBouquetService diyBouquetService;
    private final OrderService orderService;
    private final FlowerService flowerService;
    private final PackageTypeService packageTypeService;

    public DiyController(DiyBouquetService diyBouquetService, OrderService orderService,
                         FlowerService flowerService, PackageTypeService packageTypeService) {
        this.diyBouquetService = diyBouquetService;
        this.orderService = orderService;
        this.flowerService = flowerService;
        this.packageTypeService = packageTypeService;
    }

    @PostMapping("/save")
    public ResponseResult<DiyBouquet> save(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = AuthContext.getUserId(request);
        String name = (String) params.get("name");
        String packageType = (String) params.get("packageType");
        BigDecimal totalPrice = new BigDecimal(params.get("totalPrice").toString());

        DiyBouquet bouquet = new DiyBouquet();
        bouquet.setUserId(userId);
        bouquet.setName(name != null ? name : "我的花束");
        bouquet.setPackageType(packageType);
        bouquet.setTotalPrice(totalPrice);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> itemsMap = (List<Map<String, Object>>) params.get("items");
        List<DiyBouquetItem> items = new ArrayList<>();
        if (itemsMap != null) {
            for (Map<String, Object> m : itemsMap) {
                DiyBouquetItem item = new DiyBouquetItem();
                item.setFlowerId(Long.valueOf(m.get("flowerId").toString()));
                item.setFlowerName((String) m.get("flowerName"));
                item.setQuantity(Integer.valueOf(m.get("quantity").toString()));
                if (m.get("position") != null) {
                    item.setPosition(m.get("position").toString());
                }
                items.add(item);
            }
        }

        DiyBouquet saved = diyBouquetService.createBouquet(bouquet, items);
        return ResponseResult.success(saved);
    }

    @GetMapping("/list")
    public ResponseResult<List<DiyBouquet>> list(@RequestParam(required = false) Long userId,
                                                 HttpServletRequest request) {
        try {
            if (AuthContext.isAdmin(request) && userId == null) {
                return ResponseResult.success(diyBouquetService.listAllBouquets());
            }
            Long requestedUserId = userId != null ? userId : AuthContext.getUserId(request);
            AuthContext.requireOwnerOrAdmin(request, requestedUserId);
            log.info("DiyController.list called with userId={}", userId);
            List<DiyBouquet> result = diyBouquetService.listBouquetsByUser(requestedUserId);
            log.info("DiyController.list result size={}", result != null ? result.size() : 0);
            return ResponseResult.success(result);
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            log.error("DiyController.list error", e);
            return ResponseResult.error(e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseResult<Map<String, Object>> detail(@PathVariable Long id, HttpServletRequest request) {
        DiyBouquet bouquet = getAuthorizedBouquet(id, request);
        List<DiyBouquetItem> items = diyBouquetService.getBouquetItems(id);
        Map<String, Object> result = new HashMap<>();
        result.put("bouquet", bouquet);
        result.put("items", items);
        return ResponseResult.success(result);
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id, HttpServletRequest request) {
        getAuthorizedBouquet(id, request);
        return ResponseResult.success(diyBouquetService.deleteBouquet(id));
    }

    @PostMapping("/{id}/order")
    public ResponseResult<Order> placeOrder(@PathVariable Long id, @RequestBody Map<String, Object> params,
                                            HttpServletRequest request) {
        DiyBouquet bouquet = diyBouquetService.getBouquetById(id);
        if (bouquet == null) {
            throw new BaseException(404, "花束方案不存在");
        }
        AuthContext.requireOwner(request, bouquet.getUserId());
        Long userId = AuthContext.getUserId(request);
        String shippingAddress = (String) params.get("shippingAddress");
        String receiverName = (String) params.get("receiverName");
        String receiverPhone = (String) params.get("receiverPhone");

        List<DiyBouquetItem> diyItems = diyBouquetService.getBouquetItems(id);
        List<OrderItem> orderItems = diyItems.stream().map(di -> {
            OrderItem oi = new OrderItem();
            oi.setFlowerId(di.getFlowerId());
            oi.setQuantity(di.getQuantity());
            return oi;
        }).toList();

        Order order = orderService.createOrder(userId, orderItems, shippingAddress, receiverName, receiverPhone);

        bouquet.setStatus("ordered");
        diyBouquetService.updateBouquet(bouquet);

        return ResponseResult.success(order);
    }

    @GetMapping("/flowers")
    public ResponseResult<List<Flower>> flowers() {
        List<Flower> list = flowerService.listFlowers(null, null);
        for (Flower flower : list) {
            flower.setImageUrl("/images/单只" + flower.getName() + ".png");
        }
        return ResponseResult.success(list);
    }

    @GetMapping("/package/list")
    public ResponseResult<List<PackageType>> packages() {
        return ResponseResult.success(packageTypeService.listAll());
    }

    private DiyBouquet getAuthorizedBouquet(Long id, HttpServletRequest request) {
        DiyBouquet bouquet = diyBouquetService.getBouquetById(id);
        if (bouquet == null) {
            throw new BaseException(404, "花束方案不存在");
        }
        AuthContext.requireOwnerOrAdmin(request, bouquet.getUserId());
        return bouquet;
    }
}
