package com.kovanlabs.project.repository;

import com.kovanlabs.project.model.StockRequest;
import com.kovanlabs.project.model.StockRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockRequestRepository extends JpaRepository<StockRequest, Long> {
    List<StockRequest> findByBusinessIdAndStatusOrderByIdDesc(Long businessId, StockRequestStatus status);

    boolean existsByBranchIdAndIngredientNameIgnoreCaseAndStatus(
            Long branchId,
            String ingredientName,
            StockRequestStatus status
    );
}
