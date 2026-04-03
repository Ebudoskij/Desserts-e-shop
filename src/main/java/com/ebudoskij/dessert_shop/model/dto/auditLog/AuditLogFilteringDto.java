package com.ebudoskij.dessert_shop.model.dto.auditLog;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AuditLogFilteringDto {
    private String searchQuery;

    private String actionType;

    private String entityType;

    private Long entityId;

    private LocalDateTime minDate;

    private LocalDateTime maxDate;

    private String timezone;
}

