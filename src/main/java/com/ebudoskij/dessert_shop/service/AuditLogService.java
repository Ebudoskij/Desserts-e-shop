package com.ebudoskij.dessert_shop.service;

import com.ebudoskij.dessert_shop.model.dto.PageResponseDto;
import com.ebudoskij.dessert_shop.model.dto.auditLog.AuditLogFilteringDto;
import com.ebudoskij.dessert_shop.model.dto.auditLog.AuditLogResponseDto;
import org.springframework.data.domain.Pageable;

public interface AuditLogService {
    PageResponseDto<AuditLogResponseDto> getAll(AuditLogFilteringDto filter, Pageable pageable);

    /**
     * Exports all audit log rows matching {@code filter} (no pagination) as an XLSX byte array.
     * The resulting file mirrors the columns displayed in the auditLogs page.
     */
    byte[] exportToExcel(AuditLogFilteringDto filter);
}
