package com.flower.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.flower.entity.PackageType;
import com.flower.exception.BaseException;
import com.flower.mapper.PackageTypeMapper;
import com.flower.service.PackageTypeService;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class PackageTypeServiceImpl implements PackageTypeService {
    private static final Map<String, String> PACKAGE_NAME_ALIASES = Map.of(
        "圆形包装", "米白牛皮纸韩式包装",
        "心形包装", "豆沙粉雾面纸包装",
        "长形包装", "雾绿森系韩式包装",
        "礼盒包装", "紫灰礼赠纸艺包装"
    );

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
    public PackageType getEnabledByCompatibleName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new BaseException(400, "包装类型不能为空");
        }
        String requestedName = name.trim();
        Set<String> candidates = new LinkedHashSet<>();
        candidates.add(requestedName);
        String alias = PACKAGE_NAME_ALIASES.get(requestedName);
        if (alias != null) candidates.add(alias);
        PACKAGE_NAME_ALIASES.forEach((oldName, newName) -> {
            if (newName.equals(requestedName)) candidates.add(oldName);
        });

        LambdaQueryWrapper<PackageType> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(PackageType::getName, candidates);
        List<PackageType> matches = packageTypeMapper.selectList(wrapper);
        if (matches.isEmpty()) {
            throw new BaseException(404, "包装类型不存在");
        }
        return matches.stream()
            .filter(item -> "1".equals(item.getStatus()))
            .sorted((left, right) -> Boolean.compare(
                !requestedName.equals(left.getName()), !requestedName.equals(right.getName())))
            .findFirst()
            .orElseThrow(() -> new BaseException(400, "包装类型已停用"));
    }

    @Override
    public boolean add(PackageType packageType) { return packageTypeMapper.insert(packageType) > 0; }

    @Override
    public boolean update(PackageType packageType) { return packageTypeMapper.updateById(packageType) > 0; }

    @Override
    public boolean delete(Long id) { return packageTypeMapper.deleteById(id) > 0; }
}
