package com.kovanlabs.project.repository;

import com.kovanlabs.project.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByNameIgnoreCase(String name);
    Optional<Product> findByNameIgnoreCaseAndBusinessId(String name, Long businessId);
    List<Product> findByBusinessId(Long businessId);
    List<Product> findByBranchId(Long branchId);
    List<Product> findByBranchIdAndBusinessId(Long branchId, Long businessId);
    List<Product> findByBranchIsNullAndBusinessId(Long businessId);
    List<Product> findByBusinessIsNull();
    long countByBusinessId(Long businessId);
}