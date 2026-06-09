package com.flower.controller;

import com.flower.base.ResponseResult;
import com.flower.entity.DiyBouquet;
import com.flower.entity.DiyBouquetItem;
import com.flower.entity.Flower;
import com.flower.entity.Order;
import com.flower.entity.OrderItem;
import com.flower.entity.PackageType;
import com.flower.service.DiyBouquetService;
import com.flower.service.FlowerService;
import com.flower.service.OrderService;
import com.flower.service.PackageTypeService;
import lombok.extern.slf4j.Slf4j;
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
    public ResponseResult<DiyBouquet> save(@RequestBody Map<String, Object> params) {
        Long userId = Long.valueOf(params.get("userId").toString());
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
    public ResponseResult<List<DiyBouquet>> list(@RequestParam Long userId) {
        try {
            log.info("DiyController.list called with userId={}", userId);
            List<DiyBouquet> result = diyBouquetService.listBouquetsByUser(userId);
            log.info("DiyController.list result size={}", result != null ? result.size() : 0);
            return ResponseResult.success(result);
        } catch (Exception e) {
            log.error("DiyController.list error", e);
            return ResponseResult.error(e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseResult<Map<String, Object>> detail(@PathVariable Long id) {
        DiyBouquet bouquet = diyBouquetService.getBouquetById(id);
        List<DiyBouquetItem> items = diyBouquetService.getBouquetItems(id);
        Map<String, Object> result = new HashMap<>();
        result.put("bouquet", bouquet);
        result.put("items", items);
        return ResponseResult.success(result);
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.success(diyBouquetService.deleteBouquet(id));
    }

    @PostMapping("/{id}/order")
    public ResponseResult<Order> placeOrder(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Long userId = Long.valueOf(params.get("userId").toString());
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

        DiyBouquet bouquet = diyBouquetService.getBouquetById(id);
        if (bouquet != null) {
            bouquet.setStatus("ordered");
            diyBouquetService.updateBouquet(bouquet);
        }

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
}