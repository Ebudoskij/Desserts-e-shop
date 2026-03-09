package com.ebudoskij.dessert_shop.mapper;

import com.ebudoskij.dessert_shop.model.AuditLog;
import com.ebudoskij.dessert_shop.model.dto.auditLog.AuditLogResponseDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuditLogMapper {
    AuditLogResponseDto toDto(AuditLog entity);
}
