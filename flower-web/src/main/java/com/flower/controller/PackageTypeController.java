package com.flower.controller;

import com.flower.base.ResponseResult;
import com.flower.entity.PackageType;
import com.flower.service.PackageTypeService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/package")
public class PackageTypeController {
    private final PackageTypeService packageTypeService;
    public PackageTypeController(PackageTypeService packageTypeService) { this.packageTypeService = packageTypeService; }

    @GetMapping("/list")
    public ResponseResult<List<PackageType>> listAll() { return ResponseResult.success(packageTypeService.listAll()); }

    @GetMapping("/{id}")
    public ResponseResult<PackageType> getById(@PathVariable Long id) { return ResponseResult.success(packageTypeService.getById(id)); }

    @PostMapping
    public ResponseResult<Boolean> add(@RequestBody PackageType packageType) { return ResponseResult.success(packageTypeService.add(packageType)); }

    @PutMapping
    public ResponseResult<Boolean> update(@RequestBody PackageType packageType) { return ResponseResult.success(packageTypeService.update(packageType)); }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) { return ResponseResult.success(packageTypeService.delete(id)); }
}