package com.flower.service;

import com.flower.entity.PackageType;
import java.util.List;

public interface PackageTypeService {
    List<PackageType> listAll();
    PackageType getById(Long id);
    boolean add(PackageType packageType);
    boolean update(PackageType packageType);
    boolean delete(Long id);
}