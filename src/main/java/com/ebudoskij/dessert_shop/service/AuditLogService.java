package com.ebudoskij.dessert_shop.service;

import com.ebudoskij.dessert_shop.model.dto.PageResponseDto;
import com.ebudoskij.dessert_shop.model.dto.auditLog.AuditLogFilteringDto;
import com.ebudoskij.dessert_shop.model.dto.auditLog.AuditLogResponseDto;
import org.springframework.data.domain.Pageable;

public interface AuditLogService {
    PageResponseDto<AuditLogResponseDto> getAll(AuditLogFilteringDto filter, Pageable pageable);
}
