package com.ebudoskij.dessert_shop.service.impl;

import com.ebudoskij.dessert_shop.mapper.AuditLogMapper;
import com.ebudoskij.dessert_shop.model.AuditLog;
import com.ebudoskij.dessert_shop.model.dto.PageResponseDto;
import com.ebudoskij.dessert_shop.model.dto.auditLog.AuditLogResponseDto;
import com.ebudoskij.dessert_shop.repository.AuditLogRepository;
import com.ebudoskij.dessert_shop.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final AuditLogMapper auditLogMapper;

    @Override
    public PageResponseDto<AuditLogResponseDto> getAll(int page, int size, String sortBy, String sortDir, String searchQuery) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        Specification<AuditLog> spec = (root, query, criteriaBuilder) -> {
            if (searchQuery == null || searchQuery.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            String pattern = "%" + searchQuery.toLowerCase() + "%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("logType").as(String.class)), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("actionType").as(String.class)), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("details")), pattern)
            );
        };

        Page<AuditLog> auditLogPage = auditLogRepository.findAll(spec, pageRequest);
        List<AuditLogResponseDto> dtos = auditLogPage.getContent().stream()
                .map(auditLogMapper::toDto)
                .toList();
                
        PageResponseDto<AuditLogResponseDto> response = new PageResponseDto<>();
        response.setContent(dtos);
        response.setPageNo(auditLogPage.getNumber());
        response.setPageSize(auditLogPage.getSize());
        response.setTotalElements(auditLogPage.getTotalElements());
        response.setTotalPages(auditLogPage.getTotalPages());
        response.setLast(auditLogPage.isLast());

        return response;
    }
}
