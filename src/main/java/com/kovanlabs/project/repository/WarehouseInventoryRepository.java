package com.kovanlabs.project.repository;

import com.kovanlabs.project.model.WarehouseInventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WarehouseInventoryRepository extends JpaRepository<WarehouseInventory, Long> {

    //Optional<WarehouseInventory> findByIngredientNameAndWarehouseId(String ingredientName, Long warehouseId);

    Optional<WarehouseInventory> findByIngredientNameIgnoreCaseAndWarehouseId(String ingredientName, Long warehouseId);
}