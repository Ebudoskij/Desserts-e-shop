package com.ebudoskij.dessert_shop.model.dto.auditLog;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class AuditLogResponseDto {
    private Long id;

    private String logType;

    private String actionType;

    private String details;

    private Instant createdAt;
}
