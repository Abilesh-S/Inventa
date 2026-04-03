package com.kovanlabs.project.service;

import com.kovanlabs.project.dto.InventoryDTO;
import com.kovanlabs.project.model.Warehouse;
import com.kovanlabs.project.model.WarehouseInventory;
import com.kovanlabs.project.repository.WarehouseInventoryRepository;
import com.kovanlabs.project.repository.WarehouseRepository;
import org.springframework.stereotype.Service;

@Service
public class WarehouseInventoryService {

    private final WarehouseInventoryRepository inventoryRepository;
    private final WarehouseRepository warehouseRepository;

    public WarehouseInventoryService(WarehouseInventoryRepository inventoryRepository,
                                     WarehouseRepository warehouseRepository) {
        this.inventoryRepository = inventoryRepository;
        this.warehouseRepository = warehouseRepository;
    }

    public WarehouseInventory addOrUpdateIngredient(InventoryDTO dto) {

        Warehouse warehouse = warehouseRepository.findById(dto.getWarehouseId())
                .orElseThrow(() -> new RuntimeException("Warehouse not found"));

        WarehouseInventory inventory = inventoryRepository
                .findByIngredientNameIgnoreCaseAndWarehouseId(dto.getIngredientName(), dto.getWarehouseId())
                .orElse(null);

        if (inventory != null) {

            inventory.setQuantity(inventory.getQuantity() + dto.getQuantity());
        } else {

            inventory = new WarehouseInventory();
            inventory.setIngredientName(dto.getIngredientName());
            inventory.setQuantity(dto.getQuantity());
            inventory.setUnit(dto.getUnit());
            inventory.setWarehouse(warehouse);
        }

        return inventoryRepository.save(inventory);
    }
}