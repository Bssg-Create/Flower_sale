package com.flower.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.flower.entity.PackageType;
import com.flower.mapper.PackageTypeMapper;
import com.flower.service.PackageTypeService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PackageTypeServiceImpl implements PackageTypeService {
    private final PackageTypeMapper packageTypeMapper;

    public PackageTypeServiceImpl(PackageTypeMapper packageTypeMapper) {
        this.packageTypeMapper = packageTypeMapper;
    }

    @Override
    public List<PackageType> listAll() {
        LambdaQueryWrapper<PackageType> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PackageType::getStatus, "1");
        return packageTypeMapper.selectList(wrapper);
    }

    @Override
    public PackageType getById(Long id) { return packageTypeMapper.selectById(id); }

    @Override
    public boolean add(PackageType packageType) { return packageTypeMapper.insert(packageType) > 0; }

    @Override
    public boolean update(PackageType packageType) { return packageTypeMapper.updateById(packageType) > 0; }

    @Override
    public boolean delete(Long id) { return packageTypeMapper.deleteById(id) > 0; }
}