package com.ebudoskij.dessert_shop.controller;

import com.ebudoskij.dessert_shop.model.dto.PageResponseDto;
import com.ebudoskij.dessert_shop.model.dto.auditLog.AuditLogFilteringDto;
import com.ebudoskij.dessert_shop.model.dto.auditLog.AuditLogResponseDto;
import com.ebudoskij.dessert_shop.model.enums.AuditActionType;
import com.ebudoskij.dessert_shop.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/auditLog")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    public String fetchAll(
            @ModelAttribute("filter") AuditLogFilteringDto filter,
            @PageableDefault(size = 20, page = 0, sort = "id", direction = Sort.Direction.DESC)
            Pageable pageable,
            Model model) {

        PageResponseDto<AuditLogResponseDto> response = auditLogService.getAll(filter, pageable);

        model.addAttribute("pageResponse", response);
        model.addAttribute("filter", filter);
        model.addAttribute("actionTypes", AuditActionType.values());
        model.addAttribute("entityTypes", List.of("Product", "AdditionalItem", "Order", "Category"));

        return "auditLog/auditLogs";
    }
}
