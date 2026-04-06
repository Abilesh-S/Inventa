package com.kovanlabs.project.repository;

import com.kovanlabs.project.model.AlertStatus;
import com.kovanlabs.project.model.LowStockAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LowStockAlertRepository extends JpaRepository<LowStockAlert, Long> {
    boolean existsByBranchIdAndIngredientNameIgnoreCaseAndStatus(Long branchId, String ingredientName, AlertStatus status);

    Optional<LowStockAlert> findByBranchIdAndIngredientNameIgnoreCaseAndStatus(Long branchId, String ingredientName, AlertStatus status);

    List<LowStockAlert> findByBusinessIdAndStatusOrderByIdDesc(Long businessId, AlertStatus status);

    List<LowStockAlert> findByBranchIdAndStatusOrderByIdDesc(Long branchId, AlertStatus status);
}
