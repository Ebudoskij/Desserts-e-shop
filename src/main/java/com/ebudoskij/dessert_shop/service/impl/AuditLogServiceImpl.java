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
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private static final DateTimeFormatter EXPORT_DATE_FMT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss").withZone(ZoneId.systemDefault());

    private static final String[] HEADERS = {
            "ID", "Тип сутності", "ID сутності", "Тип дії",
            "Користувач", "Деталі", "Зміни", "IP-адреса", "Створено"
    };

    // ── Theme colours (brown/coffee palette matching the app) ──
    private static final byte[] HEADER_BG  = hexToRgb("6B4F35");
    private static final byte[] ALT_ROW_BG = hexToRgb("FDF8F3");

    private final AuditLogRepository auditLogRepository;
    private final AuditLogMapper auditLogMapper;

    // ──────────────────────────────────────────────────────────────
    // Paged listing
    // ──────────────────────────────────────────────────────────────

    @Override
    public PageResponseDto<AuditLogResponseDto> getAll(AuditLogFilteringDto filter, Pageable pageable) {
        Specification<AuditLog> spec = AuditLogSpecificationUtil.buildFilters(filter);
        Page<AuditLog> page = auditLogRepository.findAll(spec, pageable);

        List<AuditLogResponseDto> dtos = page.stream()
                .map(al -> {
                    AuditLogResponseDto dto = auditLogMapper.toDto(al);
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

    // ──────────────────────────────────────────────────────────────
    // Excel export
    // ──────────────────────────────────────────────────────────────

    @Override
    public byte[] exportToExcel(AuditLogFilteringDto filter) {
        Specification<AuditLog> spec = AuditLogSpecificationUtil.buildFilters(filter);
        // Fetch all matching rows ordered newest-first (no page limit)
        List<AuditLog> logs = auditLogRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "id"));

        try (XSSFWorkbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            XSSFSheet sheet = wb.createSheet("Журнал Аудиту");
            sheet.createFreezePane(0, 1);   // freeze header row

            // ── Styles ──
            CellStyle headerStyle  = buildHeaderStyle(wb);
            CellStyle altRowStyle  = buildAltRowStyle(wb);
            CellStyle wrapStyle    = buildWrapStyle(wb);
            CellStyle altWrapStyle = buildAltWrapStyle(wb);
            CellStyle dateStyle    = buildDateStyle(wb);
            CellStyle altDateStyle = buildAltDateStyle(wb);

            // ── Header row ──
            Row headerRow = sheet.createRow(0);
            headerRow.setHeightInPoints(22);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            // ── Data rows ──
            int rowNum = 1;
            for (AuditLog log : logs) {
                boolean isAlt        = (rowNum % 2 == 0);
                CellStyle base       = isAlt ? altRowStyle  : null;
                CellStyle wrapBase   = isAlt ? altWrapStyle : wrapStyle;
                CellStyle dateBase   = isAlt ? altDateStyle : dateStyle;

                Row row = sheet.createRow(rowNum++);
                row.setHeightInPoints(18);

                // 0 – ID
                createCell(row, 0, log.getId() != null ? log.getId().toString() : "", base);

                // 1 – Entity Type
                createCell(row, 1, nvl(log.getEntityType()), base);

                // 2 – Entity ID
                createCell(row, 2, log.getEntityId() != null ? log.getEntityId().toString() : "", base);

                // 3 – Action Type
                createCell(row, 3, log.getActionType() != null ? log.getActionType().name() : "", base);

                // 4 – User
                createCell(row, 4,
                        log.getUser() != null ? log.getUser().getEmail() : "system",
                        base);

                // 5 – Details
                createCell(row, 5, nvl(log.getDetails()), wrapBase);

                // 6 – Changes (newline-separated)
                List<String> changeLines = formatChanges(log.getChanges());
                String changesText = String.join("\n", changeLines);
                Cell changesCell = row.createCell(6);
                changesCell.setCellValue(changesText);
                changesCell.setCellStyle(wrapBase);
                if (changeLines.size() > 1) {
                    row.setHeightInPoints(changeLines.size() * 14f);
                }

                // 7 – IP
                createCell(row, 7, nvl(log.getIpAddress()), base);

                // 8 – Created At
                String dateStr = log.getCreatedAt() != null
                        ? EXPORT_DATE_FMT.format(log.getCreatedAt()) : "";
                createCell(row, 8, dateStr, dateBase);
            }

            // ── Auto-size columns (except Changes which can be long) ──
            int[] autoSizeCols = {0, 1, 2, 3, 4, 7, 8};
            for (int col : autoSizeCols) {
                sheet.autoSizeColumn(col);
                // Add a small padding
                sheet.setColumnWidth(col, sheet.getColumnWidth(col) + 512);
            }
            sheet.setColumnWidth(5, 10_000); // Details
            sheet.setColumnWidth(6, 14_000); // Changes

            // ── Auto-filter on header ──
            sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, HEADERS.length - 1));

            wb.write(out);
            return out.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Excel export", e);
        }
    }

    // ──────────────────────────────────────────────────────────────
    // Shared helpers
    // ──────────────────────────────────────────────────────────────

    /**
     * Renders a {@code changes} JSON node into human-readable strings.
     * Handles both diff ({@code {"field":{"from":"X","to":"Y"}}}) and
     * snapshot ({@code {"field":"value"}}) formats.
     */
    private List<String> formatChanges(JsonNode changes) {
        if (changes == null || changes.isEmpty()) return List.of();
        List<String> result = new ArrayList<>();
        changes.fieldNames().forEachRemaining(field -> {
            JsonNode node = changes.get(field);
            if (node.isObject() && node.has("from") && node.has("to")) {
                result.add(field + ": " + node.get("from").asText("null")
                        + " → " + node.get("to").asText("null"));
            } else {
                result.add(field + ": " + node.asText());
            }
        });
        return result;
    }

    private static void createCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value != null ? value : "");
        if (style != null) cell.setCellStyle(style);
    }

    private static String nvl(String value) {
        return value != null ? value : "";
    }

    // ── POI Style Builders ──

    private CellStyle buildHeaderStyle(XSSFWorkbook wb) {
        XSSFCellStyle s = wb.createCellStyle();
        XSSFColor bg = new XSSFColor(HEADER_BG, null);
        s.setFillForegroundColor(bg);
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setBorderBottom(BorderStyle.THIN);
        s.setBottomBorderColor(IndexedColors.WHITE.getIndex());
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setVerticalAlignment(VerticalAlignment.CENTER);

        XSSFFont font = wb.createFont();
        font.setBold(true);
        font.setColor(new XSSFColor(new byte[]{(byte)255, (byte)255, (byte)255}, null));
        font.setFontHeightInPoints((short) 11);
        s.setFont(font);
        return s;
    }

    private CellStyle buildAltRowStyle(XSSFWorkbook wb) {
        XSSFCellStyle s = wb.createCellStyle();
        s.setFillForegroundColor(new XSSFColor(ALT_ROW_BG, null));
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        s.setBorderBottom(BorderStyle.HAIR);
        s.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        return s;
    }

    private CellStyle buildWrapStyle(XSSFWorkbook wb) {
        CellStyle s = wb.createCellStyle();
        s.setWrapText(true);
        s.setVerticalAlignment(VerticalAlignment.TOP);
        s.setBorderBottom(BorderStyle.HAIR);
        s.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        return s;
    }

    private CellStyle buildAltWrapStyle(XSSFWorkbook wb) {
        XSSFCellStyle s = wb.createCellStyle();
        s.setFillForegroundColor(new XSSFColor(ALT_ROW_BG, null));
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setWrapText(true);
        s.setVerticalAlignment(VerticalAlignment.TOP);
        s.setBorderBottom(BorderStyle.HAIR);
        s.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        return s;
    }

    private CellStyle buildDateStyle(XSSFWorkbook wb) {
        CellStyle s = wb.createCellStyle();
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        s.setBorderBottom(BorderStyle.HAIR);
        s.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        XSSFFont mono = wb.createFont();
        mono.setFontName("Courier New");
        mono.setFontHeightInPoints((short) 9);
        s.setFont(mono);
        return s;
    }

    private CellStyle buildAltDateStyle(XSSFWorkbook wb) {
        XSSFCellStyle s = wb.createCellStyle();
        s.setFillForegroundColor(new XSSFColor(ALT_ROW_BG, null));
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        s.setBorderBottom(BorderStyle.HAIR);
        s.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        XSSFFont mono = wb.createFont();
        mono.setFontName("Courier New");
        mono.setFontHeightInPoints((short) 9);
        s.setFont(mono);
        return s;
    }

    private static byte[] hexToRgb(String hex) {
        int r = Integer.parseInt(hex.substring(0, 2), 16);
        int g = Integer.parseInt(hex.substring(2, 4), 16);
        int b = Integer.parseInt(hex.substring(4, 6), 16);
        return new byte[]{(byte) r, (byte) g, (byte) b};
    }
}
