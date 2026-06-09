package com.flower.controller;

import com.flower.base.ResponseResult;
import com.flower.entity.DiyBouquet;
import com.flower.entity.User;
import com.flower.service.DiyBouquetService;
import com.flower.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin/diy")
public class AdminDiyController {

    private final DiyBouquetService diyBouquetService;
    private final UserService userService;

    public AdminDiyController(DiyBouquetService diyBouquetService, UserService userService) {
        this.diyBouquetService = diyBouquetService;
        this.userService = userService;
    }

    @GetMapping("/list")
    public ResponseResult<List<Map<String, Object>>> list() {
        try {
            List<DiyBouquet> bouquets = diyBouquetService.listAllBouquets();
            List<User> users = userService.listAll();
            Map<Long, String> userMap = new HashMap<>();
            for (User u : users) {
                userMap.put(u.getId(), u.getUsername());
            }

            List<Map<String, Object>> result = new ArrayList<>();
            for (DiyBouquet b : bouquets) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", b.getId());
                item.put("name", b.getName());
                item.put("userId", b.getUserId());
                item.put("username", userMap.getOrDefault(b.getUserId(), "未知用户"));
                item.put("packageType", b.getPackageType());
                item.put("totalPrice", b.getTotalPrice());
                item.put("status", b.getStatus());
                item.put("createTime", b.getCreateTime());
                result.add(item);
            }
            log.info("AdminDiyController.list result size={}", result.size());
            return ResponseResult.success(result);
        } catch (Exception e) {
            log.error("AdminDiyController.list error", e);
            return ResponseResult.error(e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    public ResponseResult<Boolean> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> params) {
        try {
            DiyBouquet bouquet = diyBouquetService.getBouquetById(id);
            if (bouquet == null) {
                return ResponseResult.error("花束方案不存在");
            }
            bouquet.setStatus(params.get("status"));
            boolean ok = diyBouquetService.updateBouquet(bouquet);
            return ResponseResult.success(ok);
        } catch (Exception e) {
            log.error("AdminDiyController.updateStatus error", e);
            return ResponseResult.error(e.getMessage());
        }
    }
}