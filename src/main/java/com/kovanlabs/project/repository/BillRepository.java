package com.kovanlabs.project.repository;

import com.kovanlabs.project.model.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BillRepository extends JpaRepository<Bill, Long> {
    Optional<Bill> findByOrderId(Long orderId);
    @Query("SELECT b FROM Bill b JOIN FETCH b.customer JOIN FETCH b.order ORDER BY b.createdAt DESC")
    List<Bill> findAllByOrderByCreatedAtDesc();

    @Query("SELECT b FROM Bill b JOIN FETCH b.customer c JOIN FETCH b.order o WHERE o.branchId = :branchId ORDER BY b.createdAt DESC")
    List<Bill> findByBranchIdOrderByCreatedAtDesc(@Param("branchId") Long branchId);

    // All bills whose createdAt is in the given range (inclusive start, exclusive end)
    List<Bill> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT b FROM Bill b JOIN FETCH b.order o WHERE o.branchId = :branchId AND b.createdAt BETWEEN :start AND :end")
    List<Bill> findByBranchIdAndCreatedAtBetween(@Param("branchId") Long branchId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
