package com.kovanlabs.project.service;

import com.kovanlabs.project.model.AuditLog;
import com.kovanlabs.project.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(String action, String entityName, String details, Long referenceId) {

        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setEntityName(entityName);
        log.setDetails(details);
        log.setReferenceId(referenceId);
        log.setTimestamp(LocalDateTime.now());


        auditLogRepository.save(log);
    }
}