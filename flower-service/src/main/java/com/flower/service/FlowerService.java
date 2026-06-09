package com.flower.service;

import com.flower.entity.Flower;
import com.flower.entity.FlowerCategory;
import java.util.List;

public interface FlowerService {
    List<Flower> listFlowers(Long categoryId, String keyword);
    Flower getFlowerById(Long id);
    boolean addFlower(Flower flower);
    boolean updateFlower(Flower flower);
    boolean deleteFlower(Long id);
    List<FlowerCategory> listCategories();
    boolean addCategory(FlowerCategory category);
    boolean updateCategory(FlowerCategory category);
    boolean deleteCategory(Long id);
}