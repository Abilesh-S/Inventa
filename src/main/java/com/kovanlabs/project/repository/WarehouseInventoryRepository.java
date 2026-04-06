package com.kovanlabs.project.repository;

import com.kovanlabs.project.model.WarehouseInventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WarehouseInventoryRepository extends JpaRepository<WarehouseInventory, Long> {

    // ✅ Get specific batch
    Optional<WarehouseInventory>
    findByIngredientNameIgnoreCaseAndWarehouseIdAndBatchNumber(
            String ingredientName, Long warehouseId, String batchNumber
    );

    // ✅ Get all inventory for a warehouse
    List<WarehouseInventory> findByWarehouseId(Long warehouseId);

    // 🔥 IMPORTANT (for FIFO - future use)
    List<WarehouseInventory>
    findByIngredientNameIgnoreCaseAndWarehouseIdOrderByExpiryDateAsc(
            String ingredientName, Long warehouseId
    );
    Optional<WarehouseInventory> findByIngredientNameIgnoreCaseAndWarehouseId(String ingredientName, Long warehouseId);
}