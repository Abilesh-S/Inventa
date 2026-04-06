package com.kovanlabs.project.repository;

import com.kovanlabs.project.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import com.kovanlabs.project.model.Role;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findByBusinessId(Long businessId);
    List<User> findByBranchIdAndRole(Long branchId, Role role);
}