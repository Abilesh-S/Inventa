package com.kovanlabs.project.repository;

import com.kovanlabs.project.model.Customer;
import com.mysql.cj.x.protobuf.MysqlxCursor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerDetailsRepository extends JpaRepository<Customer , Long> {
    public Optional<Customer> findByEmailId(String emalId);
}
