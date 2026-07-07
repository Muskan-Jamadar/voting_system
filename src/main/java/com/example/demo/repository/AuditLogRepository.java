package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.model.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    boolean existsByUsernameAndActionAndDetails(
            String username,
            String action,
            String details
    );
}