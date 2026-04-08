package com.kovanlabs.project.repository;

import com.kovanlabs.project.model.Business;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BusinessRepository extends JpaRepository<Business, Long> {
    public Optional<Business> findByNameandOwnerName(String name , String ownerName );
}