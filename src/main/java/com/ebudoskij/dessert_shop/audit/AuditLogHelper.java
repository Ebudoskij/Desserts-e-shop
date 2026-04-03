package com.ebudoskij.dessert_shop.audit;

import com.ebudoskij.dessert_shop.model.AuditLog;
import com.ebudoskij.dessert_shop.model.enums.AuditActionType;
import com.ebudoskij.dessert_shop.repository.AuditLogRepository;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Thin service for persisting {@link AuditLog} entries.
 * <p>
 * Injected by any service that needs to record an auditable event.
 * Automatically reads user and IP from {@link AuditContextHolder} (populated
 * by {@link AuditContextAspect}) so callers do not need to pass those values.
 */
@Service
@RequiredArgsConstructor
public class AuditLogHelper {

    private final AuditLogRepository auditLogRepository;

    /**
     * Persists a single audit log entry for the given entity action.
     *
     * @param entityType  Simple class name of the affected entity: "Product", "Order", etc.
     * @param entityId    Database PK of the affected entity.
     * @param actionType  The type of operation performed.
     * @param changes     Structured JSON describing what changed (diff or snapshot); may be null.
     * @param details     Human-readable summary of the event.
     */
    public void log(String entityType,
                    Long entityId,
                    AuditActionType actionType,
                    JsonNode changes,
                    String details) {

        AuditLog auditLog = new AuditLog();
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setActionType(actionType);
        auditLog.setChanges(changes);
        auditLog.setDetails(details);

        AuditContextHolder.AuditMetadata metadata = AuditContextHolder.get();
        if (metadata != null) {
            auditLog.setUser(metadata.getUser());
            auditLog.setIpAddress(metadata.getIpAddress());
        }

        auditLogRepository.save(auditLog);
    }
}
