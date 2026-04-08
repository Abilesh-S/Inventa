package com.kovanlabs.project.service;

import com.kovanlabs.project.model.*;
import com.kovanlabs.project.repository.BranchInventoryRepository;
import com.kovanlabs.project.repository.LowStockAlertRepository;
import com.kovanlabs.project.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class LowStockAlertService {

    private static final Logger logger = LoggerFactory.getLogger(LowStockAlertService.class);
    private final LowStockAlertRepository lowStockAlertRepository;
    private final UserRepository userRepository;
    private final BranchInventoryRepository branchInventoryRepository;

    public LowStockAlertService(LowStockAlertRepository lowStockAlertRepository,
                                UserRepository userRepository,
                                BranchInventoryRepository branchInventoryRepository) {
        this.lowStockAlertRepository = lowStockAlertRepository;
        this.userRepository = userRepository;
        this.branchInventoryRepository = branchInventoryRepository;
    }

    public void createIfLowStock(BranchInventory inventory) {
        logger.info("Checking low stock for ingredient: {} in branch ID: {}",
                inventory.getIngredientName(), inventory.getBranch().getId());
        if (inventory.getThreshold() == null || inventory.getQuantity() == null) {
            logger.warn("Threshold or quantity is null for ingredient: {}", inventory.getIngredientName());
            return;
        }
        if (inventory.getQuantity() > inventory.getThreshold()) {
            logger.debug("Stock is sufficient for ingredient: {} ({} > {})",
                    inventory.getIngredientName(), inventory.getQuantity(), inventory.getThreshold());
            return;
        }

        String ingredient = inventory.getIngredientName().trim().toLowerCase();
        boolean openExists = lowStockAlertRepository.existsByBranchIdAndIngredientNameIgnoreCaseAndStatus(
                inventory.getBranch().getId(),
                ingredient,
                AlertStatus.OPEN
        );
        if (openExists) {
            logger.info("Open alert already exists for ingredient: {} in branch ID: {}",
                    ingredient, inventory.getBranch().getId());
            return;
        }

        LowStockAlert alert = new LowStockAlert();
        alert.setBusiness(inventory.getBranch().getBusiness());
        alert.setBranch(inventory.getBranch());
        alert.setIngredientName(ingredient);
        alert.setCurrentQuantity(inventory.getQuantity());
        alert.setThreshold(inventory.getThreshold());
        alert.setStatus(AlertStatus.OPEN);
        alert.setMessage("Low stock: " + ingredient + " at branch " + inventory.getBranch().getName());
        lowStockAlertRepository.save(alert);
        logger.info("Low stock alert CREATED for ingredient: {} (qty: {}, threshold: {})",
                ingredient, inventory.getQuantity(), inventory.getThreshold());
    }

    public void closeIfRecovered(BranchInventory inventory) {
        logger.info("Checking recovery for ingredient: {} in branch ID: {}",
                inventory.getIngredientName(), inventory.getBranch().getId());
        if (inventory.getThreshold() == null || inventory.getQuantity() == null) {
            logger.warn("Threshold or quantity is null for ingredient: {}", inventory.getIngredientName());
            return;
        }
        if (inventory.getQuantity() <= inventory.getThreshold()) {
            logger.debug("Stock still low for ingredient: {} ({} <= {})",
                    inventory.getIngredientName(), inventory.getQuantity(), inventory.getThreshold());
            return;
        }

        lowStockAlertRepository
                .findByBranchIdAndIngredientNameIgnoreCaseAndStatus(
                        inventory.getBranch().getId(),
                        inventory.getIngredientName(),
                        AlertStatus.OPEN
                )
                .ifPresentOrElse(alert -> {
                    alert.setStatus(AlertStatus.CLOSED);
                    alert.setCurrentQuantity(inventory.getQuantity());
                    lowStockAlertRepository.save(alert);
                    logger.info("Low stock alert CLOSED for ingredient: {} (new qty: {})",
                            inventory.getIngredientName(), inventory.getQuantity());
                },() -> logger.debug("No open alert found to close for ingredient: {}",
                        inventory.getIngredientName()));

    }

    public List<LowStockAlert> getOwnerOpenAlerts(String ownerEmail) {
        logger.info("Fetching owner alerts for email: {}", ownerEmail);
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> {
                    logger.error("Owner not found with email: {}", ownerEmail);
                    return new RuntimeException("Owner not found");
                });

        if (owner.getRole() != Role.OWNER || owner.getBusiness() == null) {
            logger.error("Unauthorized access to owner alerts by: {}", ownerEmail);
            throw new RuntimeException("Only owner can view owner alerts");
        }
        List<LowStockAlert> alerts = lowStockAlertRepository.findByBusinessIdAndStatusOrderByIdDesc(
                owner.getBusiness().getId(), AlertStatus.OPEN);

        List<LowStockAlert> refreshed = refreshAndFilterOpenAlerts(alerts);
        logger.info("Found {} open alerts for owner after refresh: {}", refreshed.size(), ownerEmail);
        return refreshed;
    }

    public List<LowStockAlert> getManagerOpenAlerts(String managerEmail) {
        logger.info("Fetching manager alerts for email: {}", managerEmail);
        User manager = userRepository.findByEmail(managerEmail)
                .orElseThrow(() -> {
                    logger.error("Manager not found with email: {}", managerEmail);
                    return new RuntimeException("Manager not found");
                });

        if (manager.getRole() != Role.MANAGER || manager.getBranch() == null) {
            logger.error("Unauthorized access to manager alerts by: {}", managerEmail);
            throw new RuntimeException("Only manager can view manager alerts");
        }
        List<LowStockAlert> alerts = lowStockAlertRepository.findByBranchIdAndStatusOrderByIdDesc(
                manager.getBranch().getId(), AlertStatus.OPEN);

        List<LowStockAlert> refreshed = refreshAndFilterOpenAlerts(alerts);
        logger.info("Found {} open alerts for manager after refresh: {}", refreshed.size(), managerEmail);
        return refreshed;
    }

    private List<LowStockAlert> refreshAndFilterOpenAlerts(List<LowStockAlert> openAlerts) {
        if (openAlerts == null || openAlerts.isEmpty()) return openAlerts;

        List<LowStockAlert> result = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (LowStockAlert alert : openAlerts) {
            if (alert == null || alert.getBranch() == null) continue;
            String ingredientKey = alert.getIngredientName() == null ? "" : alert.getIngredientName().trim();
            java.util.Optional<BranchInventory> invOpt =
                    branchInventoryRepository.findByBranchIdAndIngredientNameIgnoreCase(
                            alert.getBranch().getId(), ingredientKey);

            if (invOpt.isPresent()) {
                BranchInventory inv = invOpt.get();

                alert.setCurrentQuantity(inv.getQuantity() != null ? inv.getQuantity() : 0.0);
                alert.setThreshold(inv.getThreshold() != null ? inv.getThreshold() : alert.getThreshold());
                if (inv.getIngredientName() != null) alert.setIngredientName(inv.getIngredientName().trim().toLowerCase());

                boolean expired = (inv.getExpiryDate() != null && inv.getExpiryDate().isBefore(today))
                        || ("EXPIRED".equalsIgnoreCase(inv.getStatus()));
                boolean qtyLow = inv.getQuantity() != null && inv.getThreshold() != null
                        && inv.getQuantity() > 0 && inv.getQuantity() <= inv.getThreshold();

                if (!(qtyLow || expired)) {
                    alert.setStatus(AlertStatus.CLOSED);
                    lowStockAlertRepository.save(alert);
                    continue;
                }
            } else {

                alert.setStatus(AlertStatus.CLOSED);
                lowStockAlertRepository.save(alert);
                continue;
            }


            lowStockAlertRepository.save(alert);
            result.add(alert);
        }

        return result;
    }
}
