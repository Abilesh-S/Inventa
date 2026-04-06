package com.kovanlabs.project.service;

import com.kovanlabs.project.dto.InventoryDTO;
import com.kovanlabs.project.dto.WarehouseInventoryDTO;
import com.kovanlabs.project.model.Warehouse;
import com.kovanlabs.project.model.WarehouseInventory;
import com.kovanlabs.project.repository.WarehouseInventoryRepository;
import com.kovanlabs.project.repository.WarehouseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WarehouseInventoryService {

    private static final Logger logger = LoggerFactory.getLogger(WarehouseInventoryService.class);

    private final WarehouseInventoryRepository inventoryRepository;
    private final WarehouseRepository warehouseRepository;
    private final AuditService auditService;

    public WarehouseInventoryService(WarehouseInventoryRepository inventoryRepository,
                                     WarehouseRepository warehouseRepository,
                                     AuditService auditService) {
        this.inventoryRepository = inventoryRepository;
        this.warehouseRepository = warehouseRepository;
        this.auditService = auditService;
    }


    public WarehouseInventory addOrUpdateIngredient(WarehouseInventoryDTO dto) {
        logger.info("Processing ingredient: {} (ID: {}) for warehouse ID: {}",
                dto.getIngredientName(), dto.getId(), dto.getWarehouseId());

        Warehouse warehouse = warehouseRepository.findById(dto.getWarehouseId())
                .orElseThrow(() -> new RuntimeException("Warehouse not found"));

        WarehouseInventory inventory = null;

        if (dto.getId() != null) {
            inventory = inventoryRepository.findById(dto.getId()).orElse(null);
        }


        if (inventory == null && dto.getIngredientName() != null) {
            String name = dto.getIngredientName().trim().toLowerCase();
            inventory = inventoryRepository.findByIngredientNameIgnoreCaseAndWarehouseId(
                    name, dto.getWarehouseId()).orElse(null);
        }

        if (inventory != null) {

            logger.info("Existing ingredient found: {} (ID: {}). Updating...",
                    inventory.getIngredientName(), inventory.getId());

            if (dto.getId() != null) {

                inventory.setQuantity(dto.getQuantity());
            } else {

                inventory.setQuantity(inventory.getQuantity() + dto.getQuantity());
            }


            if (dto.getThreshold() != null) inventory.setThreshold(dto.getThreshold());
            if (dto.getUnit() != null) inventory.setUnit(dto.getUnit());
            if (dto.getIngredientName() != null)
                inventory.setIngredientName(dto.getIngredientName().trim().toLowerCase());

            auditService.log("STOCK_UPDATED", "WarehouseInventory",
                    (dto.getId() != null ? "Updated " : "Merged stock for ") + dto.getIngredientName(),
                    inventory.getId());
        } else {
            logger.info("No existing ingredient found for name: {}. Creating new record.", dto.getIngredientName());
            inventory = new WarehouseInventory();
            inventory.setIngredientName(dto.getIngredientName().trim().toLowerCase());
            inventory.setQuantity(dto.getQuantity());
            inventory.setThreshold(dto.getThreshold());
            inventory.setUnit(dto.getUnit());
            inventory.setWarehouse(warehouse);
        }

        inventory.setBatchNumber(dto.getBatchNumber());
        inventory.setExpiryDate(dto.getExpiryDate());
        inventory.setStatus(dto.getStatus());

        return inventoryRepository.save(inventory);
    }

    public List<WarehouseInventory> getAllIngredients() {
        return inventoryRepository.findAll();
    }

    public List<WarehouseInventory> getAllIngredients(Long warehouseId) {
        logger.info("Fetching all ingredients for warehouse ID: {}", warehouseId);
        return inventoryRepository.findByWarehouseId(warehouseId);
    }

    public WarehouseInventory getIngredient(Long warehouseId, String name, String batch) {

        logger.info("Fetching ingredient: {} batch: {} for warehouse ID: {}",
                name, batch, warehouseId);

        return inventoryRepository
                .findByIngredientNameIgnoreCaseAndWarehouseIdAndBatchNumber(
                        name.trim().toLowerCase(),
                        warehouseId,
                        batch
                )
                .orElseThrow(() -> new RuntimeException("Ingredient not found"));
    }

    public void deleteIngredient(Long id) {
        logger.info("Deleting ingredient with ID: {}", id);
        WarehouseInventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ingredient not found with ID: " + id));
        inventoryRepository.delete(inventory);
        logger.info("Ingredient with ID: {} deleted successfully", id);
    }


    public String deleteIngredient(Long warehouseId, String name, String batch) {

        WarehouseInventory inventory = getIngredient(warehouseId, name, batch);
        inventoryRepository.delete(inventory);

        logger.info("Ingredient deleted successfully: {}", name);

        return "Ingredient deleted successfully";
    }

    public List<String> getUniqueIngredientNames() {
        return inventoryRepository.findAll().stream()
                .map(WarehouseInventory::getIngredientName)
                .distinct()
                .toList();
    }
}