package com.kovanlabs.project.service;

import com.kovanlabs.project.dto.BranchInventoryDTO;
import com.kovanlabs.project.model.*;
import com.kovanlabs.project.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

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

        BranchInventory inventory = null;
        boolean isExplicitEdit = false;
        if (dto.getId() != null) {
            inventory = branchInventoryRepository.findById(dto.getId()).orElse(null);
            if (inventory != null) isExplicitEdit = true;
        }


        if (inventory == null) {
            inventory = branchInventoryRepository
                    .findByBranchIdAndIngredientNameIgnoreCase(branch.getId(), name)
                    .orElse(new BranchInventory());
        }

        if (inventory.getId() != null) {
            if (isExplicitEdit) {
                logger.info("Explicit Edit (ID: {}). Setting quantity to {}", inventory.getId(), dto.getQuantity());
                inventory.setQuantity(dto.getQuantity()); // SET for edits
                auditService.log("STOCK_UPDATED", "BranchInventory", "Edited " + name + " to " + dto.getQuantity(), inventory.getId());
            } else {
                logger.info("Name-based Merge (Name: {}). Adding {} to existing {}", name, dto.getQuantity(), inventory.getQuantity());
                inventory.setQuantity(inventory.getQuantity() + dto.getQuantity()); // INCREMENT for duplicates
                auditService.log("STOCK_UPDATED", "BranchInventory", "Merged " + name + " added " + dto.getQuantity(), inventory.getId());
            }
        } else {
            logger.info("Creating new ingredient record: {}", name);
            inventory.setQuantity(dto.getQuantity());
            inventory.setBranch(branch);
            inventory.setPricePerUnit(dto.getPricePerUnit());
            inventory.setThreshold(dto.getThreshold());
            inventory.setUnit(dto.getUnit());
            inventory.setUnit(dto.getUnit());
        }

        inventory.setIngredientName(name);
        if (dto.getPricePerUnit() != null) {
            inventory.setPricePerUnit(dto.getPricePerUnit());
        }
        inventory.setBatchNumber(dto.getBatchNumber());
        inventory.setExpiryDate(dto.getExpiryDate());
        // IMPORTANT: Orders only deduct from rows with status="ACTIVE".
        // If the frontend doesn't send status, default to ACTIVE so stock stays usable.
        inventory.setStatus(dto.getStatus() == null || dto.getStatus().isBlank() ? "ACTIVE" : dto.getStatus());

        boolean isNew = (inventory.getId() == null);
        BranchInventory savedInventory = branchInventoryRepository.save(inventory);

        if (isNew) {
            auditService.log("STOCK_ADDED", "BranchInventory", "New ingredient " + name + " qty " + dto.getQuantity(), savedInventory.getId());
        }

        logger.info("Ingredient saved successfully: {}", savedInventory);
        return savedInventory;
    }


    public List<BranchInventory> getAllIngredients(Long branchId) {
        logger.info("Fetching all ingredients for branch ID: {}", branchId);
        return branchInventoryRepository.findByBranchId(branchId);
    }

    /** Sum of ingredient quantities across every branch belonging to the business. */
    public double getTotalQuantityAcrossBranches(Long businessId) {
        return branchInventoryRepository.findByBranch_Business_Id(businessId).stream()
                .mapToDouble(bi -> Objects.requireNonNullElse(bi.getQuantity(), 0.0))
                .sum();
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

    public void deleteById(Long id) {
        logger.info("Deleting branch inventory record by ID: {}", id);
        branchInventoryRepository.deleteById(id);
    }
}