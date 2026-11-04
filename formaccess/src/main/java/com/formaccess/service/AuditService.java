package com.formaccess.service;

import com.formaccess.entity.AuditLog;
import com.formaccess.entity.Event;
import com.formaccess.entity.User;
import com.formaccess.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

/** Writes rows into event_audit_logs. */
@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(Event event, User actor, String action, String details) {
        AuditLog log = new AuditLog();
        log.setEvent(event);
        log.setActor(actor);
        log.setAction(action);
        log.setDetails(details == null || details.length() <= 1000 ? details : details.substring(0, 1000));
        auditLogRepository.save(log);
    }
}
