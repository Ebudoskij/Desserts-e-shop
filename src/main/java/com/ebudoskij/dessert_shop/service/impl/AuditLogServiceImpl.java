package com.ebudoskij.dessert_shop.service.impl;

import com.ebudoskij.dessert_shop.mapper.AuditLogMapper;
import com.ebudoskij.dessert_shop.model.AuditLog;
import com.ebudoskij.dessert_shop.model.dto.PageResponseDto;
import com.ebudoskij.dessert_shop.model.dto.auditLog.AuditLogFilteringDto;
import com.ebudoskij.dessert_shop.model.dto.auditLog.AuditLogResponseDto;
import com.ebudoskij.dessert_shop.repository.AuditLogRepository;
import com.ebudoskij.dessert_shop.service.AuditLogService;
import com.ebudoskij.dessert_shop.utils.specifications.AuditLogSpecificationUtil;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final AuditLogMapper auditLogMapper;

    @Override
    public PageResponseDto<AuditLogResponseDto> getAll(AuditLogFilteringDto filter, Pageable pageable) {
        Specification<AuditLog> spec = AuditLogSpecificationUtil.buildFilters(filter);

        Page<AuditLog> page = auditLogRepository.findAll(spec, pageable);

        List<AuditLogResponseDto> dtos = page.stream()
                .map(al -> {
                    AuditLogResponseDto dto = auditLogMapper.toDto(al);

                    // Null-safe: system-triggered actions have no user
                    dto.setUserEmail(al.getUser() != null ? al.getUser().getEmail() : "system");
                    dto.setFormattedChanges(formatChanges(al.getChanges()));
                    return dto;
                })
                .toList();

        PageResponseDto<AuditLogResponseDto> pageResponse = new PageResponseDto<>();
        pageResponse.setPageSize(page.getSize());
        pageResponse.setSort(page.getSort());
        pageResponse.setPageNo(page.getNumber());
        pageResponse.setTotalPages(page.getTotalPages());
        pageResponse.setTotalElements(page.getTotalElements());
        pageResponse.setLast(page.isLast());
        pageResponse.setContent(dtos);

        return pageResponse;
    }

    /**
     * Renders the {@code changes} JSON into human-readable strings.
     *
     * <p>Handles two JSON shapes:
     * <ul>
     *   <li><b>Diff format</b> (UPDATED / STATUS_CHANGED / DELETED / RESTORED):
     *       {@code {"field": {"from": "X", "to": "Y"}}} → {@code "field: X → Y"}</li>
     *   <li><b>Snapshot format</b> (CREATED):
     *       {@code {"field": "value"}} → {@code "field: value"}</li>
     * </ul>
     */
    private List<String> formatChanges(JsonNode changes) {
        if (changes == null || changes.isEmpty()) return List.of();

        List<String> formatted = new ArrayList<>();
        changes.fieldNames().forEachRemaining(fieldName -> {
            JsonNode node = changes.get(fieldName);
            if (node.isObject() && node.has("from") && node.has("to")) {
                // Diff format
                String oldVal = node.get("from").asText("null");
                String newVal = node.get("to").asText("null");
                formatted.add(fieldName + ": " + oldVal + " → " + newVal);
            } else {
                // Snapshot format (CREATED)
                formatted.add(fieldName + ": " + node.asText());
            }
        });
        return formatted;
    }
}
