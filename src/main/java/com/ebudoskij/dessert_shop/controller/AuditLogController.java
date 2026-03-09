package com.ebudoskij.dessert_shop.controller;

import com.ebudoskij.dessert_shop.model.dto.PageResponseDto;
import com.ebudoskij.dessert_shop.model.dto.auditLog.AuditLogResponseDto;
import com.ebudoskij.dessert_shop.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/auditLog")
@RequiredArgsConstructor
public class AuditLogController {
    private final AuditLogService auditLogService;

    @GetMapping
    public String fetchAll(@RequestParam(required = false, defaultValue = "0") int page,
                           @RequestParam(required = false, defaultValue = "10") int size,
                           @RequestParam(required = false, defaultValue = "id") String sortBy,
                           @RequestParam(required = false, defaultValue = "asc") String sortDir,
                           @RequestParam(required = false) String searchQuery,
                           Model model){
        PageResponseDto<AuditLogResponseDto> response = auditLogService.getAll(
                page,
                size,
                sortBy,
                sortDir,
                searchQuery);

        model.addAttribute("pageResponse", response);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("searchQuery", searchQuery);

        return "auditLog/auditLogs";
    }
}
