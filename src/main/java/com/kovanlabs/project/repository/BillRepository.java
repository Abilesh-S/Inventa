package com.kovanlabs.project.repository;

import com.kovanlabs.project.model.Bill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BillRepository extends JpaRepository<Bill, Long> {
    Optional<Bill> findByOrderId(Long orderId);
    @org.springframework.data.jpa.repository.Query("SELECT b FROM Bill b JOIN FETCH b.customer JOIN FETCH b.order ORDER BY b.createdAt DESC")
    List<Bill> findAllByOrderByCreatedAtDesc();
}
