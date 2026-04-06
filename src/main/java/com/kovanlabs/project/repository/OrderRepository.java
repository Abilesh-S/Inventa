package com.kovanlabs.project.repository;

import com.kovanlabs.project.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

}