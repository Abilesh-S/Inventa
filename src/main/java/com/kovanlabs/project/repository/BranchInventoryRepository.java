package com.kovanlabs.project.repository;

import com.kovanlabs.project.model.BranchInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface BranchInventoryRepository extends JpaRepository<BranchInventory, Long> {

    List<BranchInventory> findByBranchId(Long branchId);
    Optional<BranchInventory> findByBranchIdAndIngredientNameIgnoreCase(Long branchId, String ingredientName);
    List<BranchInventory> findByBranchIdAndIngredientNameIgnoreCaseAndStatusOrderByExpiryDateAsc(
            Long branchId, String name, String status);
}