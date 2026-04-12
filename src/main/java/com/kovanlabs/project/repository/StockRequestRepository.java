package com.kovanlabs.project.repository;

import com.kovanlabs.project.model.StockRequest;
import com.kovanlabs.project.model.StockRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockRequestRepository extends JpaRepository<StockRequest, Long> {
    List<StockRequest> findByBusinessIdAndStatusOrderByIdDesc(Long businessId, StockRequestStatus status);
    List<StockRequest> findByBranchIdOrderByIdDesc(Long branchId);
    List<StockRequest> findByBranchIdAndStatusOrderByIdDesc(Long branchId, StockRequestStatus status);

    boolean existsByBranchIdAndIngredientNameIgnoreCaseAndStatus(
            Long branchId,
            String ingredientName,
            StockRequestStatus status
    );
}
