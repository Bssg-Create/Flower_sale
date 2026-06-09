package com.flower.controller;

import com.flower.base.ResponseResult;
import com.flower.entity.Flower;
import com.flower.entity.FlowerCategory;
import com.flower.service.FlowerService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/flower")
public class FlowerController {
    private final FlowerService flowerService;
    public FlowerController(FlowerService flowerService) { this.flowerService = flowerService; }

    @GetMapping("/list")
    public ResponseResult<List<Flower>> list(@RequestParam(required = false) Long categoryId,
                                              @RequestParam(required = false) String keyword) {
        return ResponseResult.success(flowerService.listFlowers(categoryId, keyword));
    }

    @GetMapping("/{id}")
    public ResponseResult<Flower> getById(@PathVariable Long id) { return ResponseResult.success(flowerService.getFlowerById(id)); }

    @PostMapping
    public ResponseResult<Boolean> add(@RequestBody Flower flower) { return ResponseResult.success(flowerService.addFlower(flower)); }

    @PutMapping
    public ResponseResult<Boolean> update(@RequestBody Flower flower) { return ResponseResult.success(flowerService.updateFlower(flower)); }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) { return ResponseResult.success(flowerService.deleteFlower(id)); }

    @GetMapping("/category/list")
    public ResponseResult<List<FlowerCategory>> listCategories() { return ResponseResult.success(flowerService.listCategories()); }

    @PostMapping("/category")
    public ResponseResult<Boolean> addCategory(@RequestBody FlowerCategory category) { return ResponseResult.success(flowerService.addCategory(category)); }

    @PutMapping("/category")
    public ResponseResult<Boolean> updateCategory(@RequestBody FlowerCategory category) { return ResponseResult.success(flowerService.updateCategory(category)); }

    @DeleteMapping("/category/{id}")
    public ResponseResult<Boolean> deleteCategory(@PathVariable Long id) { return ResponseResult.success(flowerService.deleteCategory(id)); }
}