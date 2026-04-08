package com.kovanlabs.project.repository;

import com.kovanlabs.project.model.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification , Long> {
    public Optional<EmailVerification> findByEmailId(String msg);
}
