package com.kovanlabs.project.service;

import com.kovanlabs.project.dto.BranchInventoryDTO;
import com.kovanlabs.project.model.*;
import com.kovanlabs.project.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BranchInventoryService {


    private static final Logger logger = LoggerFactory.getLogger(BranchInventoryService.class);
    private final BranchInventoryRepository branchInventoryRepository;
    private final BranchRepository branchRepository;
    private final AuditService auditService;

    public BranchInventoryService(BranchInventoryRepository branchInventoryRepository,
                                  BranchRepository branchRepository,AuditService auditService) {
        this.branchInventoryRepository = branchInventoryRepository;
        this.branchRepository = branchRepository;
        this.auditService=auditService;
    }


    public BranchInventory addOrUpdateIngredient(BranchInventoryDTO dto) {
        logger.info("Adding or updating ingredient: {} for branch ID: {}", dto.getIngredientName(), dto.getBranchId());
        Branch branch = branchRepository.findById(dto.getBranchId())
                .orElseThrow(() -> new RuntimeException("Branch not found"));
        String name = dto.getIngredientName().trim().toLowerCase();
        BranchInventory inventory = branchInventoryRepository
                .findByBranchIdAndIngredientNameIgnoreCase(branch.getId(), dto.getIngredientName())
                .orElse(new BranchInventory());

        inventory.setIngredientName(name);
        if (inventory.getId() != null) {
            logger.info("Ingredient exists. Updating quantity from {} to {}",
                    inventory.getQuantity(), inventory.getQuantity() + dto.getQuantity());
            inventory.setQuantity(inventory.getQuantity() + dto.getQuantity());
            auditService.log(
                    "STOCK_UPDATED",
                    "BranchInventory",
                    "Updated " + dto.getIngredientName() +
                            " added " + dto.getQuantity(),
                    inventory.getId()
            );

        } else {
            logger.info("Creating new ingredient: {}", name);
            inventory.setQuantity(dto.getQuantity());
            inventory.setThreshold(dto.getThreshold());
            inventory.setUnit(dto.getUnit());
            inventory.setBranch(branch);
            auditService.log(
                    "STOCK_ADDED",
                    "BranchInventory",
                    "New ingredient " + dto.getIngredientName() +
                            " qty " + dto.getQuantity(),
                    inventory.getId()
            );

        }

        inventory.setBatchNumber(dto.getBatchNumber());
        inventory.setExpiryDate(dto.getExpiryDate());
        inventory.setStatus(dto.getStatus());
        BranchInventory savedInventory = branchInventoryRepository.save(inventory);
        logger.info("Ingredient saved successfully: {}", savedInventory);


        return savedInventory;

    }


    public List<BranchInventory> getAllIngredients(Long branchId) {
        logger.info("Fetching all ingredients for branch ID: {}", branchId);
        return branchInventoryRepository.findByBranchId(branchId);
    }


    public BranchInventory getIngredient(Long branchId, String name) {
        logger.info("Fetching ingredient: {} for branch ID: {}", name, branchId);
        return branchInventoryRepository.findByBranchIdAndIngredientNameIgnoreCase(branchId, name)
                .orElseThrow(() -> new RuntimeException("Ingredient not found"));
    }


    public String deleteIngredient(Long branchId, String name) {
        BranchInventory inventory = getIngredient(branchId, name);
        branchInventoryRepository.delete(inventory);
        logger.info("Ingredient deleted successfully: {}", name);
        return "Ingredient deleted successfully";
    }
}