package com.ebudoskij.dessert_shop.model.dto.auditLog;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class AuditLogResponseDto {
    private Long id;

    private String userEmail;

    private String entityType;

    private Long entityId;

    private String actionType;

    private List<String> formattedChanges;

    private String details;

    private String ipAddress;

    private Instant createdAt;
}
