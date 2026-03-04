package com.ebudoskij.dessert_shop.service;

import com.ebudoskij.dessert_shop.model.dto.PageResponseDto;
import com.ebudoskij.dessert_shop.model.dto.auditLog.AuditLogResponseDto;

public interface AuditLogService {
    PageResponseDto<AuditLogResponseDto> getAll(int page, int size, String sortBy, String sortDir, String searchQuery);
}
