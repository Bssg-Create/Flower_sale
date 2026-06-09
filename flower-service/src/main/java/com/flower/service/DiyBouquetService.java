package com.flower.service;

import com.flower.entity.DiyBouquet;
import com.flower.entity.DiyBouquetItem;
import java.util.List;

public interface DiyBouquetService {
    DiyBouquet createBouquet(DiyBouquet bouquet, List<DiyBouquetItem> items);
    DiyBouquet getBouquetById(Long id);
    List<DiyBouquet> listBouquetsByUser(Long userId);
    List<DiyBouquet> listAllBouquets();
    boolean deleteBouquet(Long id);
    List<DiyBouquetItem> getBouquetItems(Long bouquetId);
    boolean updateBouquet(DiyBouquet bouquet);
}