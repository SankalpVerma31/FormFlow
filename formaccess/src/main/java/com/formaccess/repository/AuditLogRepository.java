package com.formaccess.repository;

import com.formaccess.entity.AuditLog;
import com.formaccess.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByEventOrderByCreatedAtDesc(Event event);
    List<AuditLog> findTop15ByOrderByCreatedAtDesc();
}
