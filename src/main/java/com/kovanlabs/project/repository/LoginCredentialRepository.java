package com.kovanlabs.project.repository;

import com.kovanlabs.project.model.LoginCredentials;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoginCredentialRepository extends JpaRepository<LoginCredentials , Long> {
    public Optional<LoginCredentials> findByUserName(String username);
}
