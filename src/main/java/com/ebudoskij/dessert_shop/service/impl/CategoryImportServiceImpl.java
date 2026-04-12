package com.ebudoskij.dessert_shop.service.impl;

import com.ebudoskij.dessert_shop.model.Category;
import com.ebudoskij.dessert_shop.model.dto.ImportResult;
import com.ebudoskij.dessert_shop.repository.CategoryRepository;
import com.ebudoskij.dessert_shop.service.CategoryImportService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CategoryImportServiceImpl implements CategoryImportService {

    private final CategoryRepository categoryRepository;

    // ──────────────────────────────────────────────────────────────────────────
    // Import
    // ──────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ImportResult importFromXlsx(MultipartFile file) throws IOException {

        List<String> errors = new ArrayList<>();

        try (Workbook wb = WorkbookFactory.create(file.getInputStream())) {

            Sheet sheet = wb.getSheetAt(0);

            // ── 1. Seed byName from DB so parentName can reference existing categories ──
            Map<String, Category> byName = new LinkedHashMap<>();
            for (Category existing : categoryRepository.findAll()) {
                byName.put(existing.getName(), existing);
            }

            // ── 2. Pass 1 — collect rows, create Category stubs ──
            // Each entry: {category, parentNameFromExcel, excelRowNumber}
            record RowEntry(Category category, String parentName, int rowNum) {}
            List<RowEntry> entries = new ArrayList<>();

            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                String name = getCellString(row, 0);
                if (name == null || name.isBlank()) continue; // skip blank-name rows

                String description = getCellString(row, 1);
                String parentName  = getCellString(row, 2);

                int excelRow = r + 1; // 1-based for humans

                // Duplicate check
                if (byName.containsKey(name)) {
                    errors.add("Рядок " + excelRow + ": категорія «" + name + "» вже існує — пропущено.");
                    continue;
                }

                Category cat = new Category();
                cat.setName(name);
                cat.setDescription(description);
                cat.setIsDeleted(false);

                byName.put(name, cat);
                entries.add(new RowEntry(cat, parentName, excelRow));
            }

            if (!errors.isEmpty()) {
                return ImportResult.failure(errors);
            }

            // ── 3. Pass 2 — wire parent references ──
            //    Handles child-before-parent row ordering because byName is already complete
            for (RowEntry entry : entries) {
                String parentName = entry.parentName();
                if (parentName != null && !parentName.isBlank()) {
                    Category parent = byName.get(parentName);
                    if (parent == null) {
                        errors.add("Рядок " + entry.rowNum() + ": батьківська категорія «"
                                + parentName + "» не знайдена.");
                    } else {
                        entry.category().setParent(parent);
                    }
                }
            }

            if (!errors.isEmpty()) {
                return ImportResult.failure(errors);
            }

            // ── 4. saveAll — roots first, then children (FK constraint) ──
            List<Category> roots    = new ArrayList<>();
            List<Category> children = new ArrayList<>();

            for (RowEntry entry : entries) {
                if (entry.category().getParent() == null) {
                    roots.add(entry.category());
                } else {
                    children.add(entry.category());
                }
            }

            categoryRepository.saveAll(roots);
            categoryRepository.saveAll(children);

            int total = roots.size() + children.size();
            return ImportResult.success(total);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Template generation
    // ──────────────────────────────────────────────────────────────────────────

    @Override
    public byte[] buildTemplate() throws IOException {
        try (Workbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = wb.createSheet("Категорії");

            // Bold font for header
            Font bold = wb.createFont();
            bold.setBold(true);

            CellStyle headerStyle = wb.createCellStyle();
            headerStyle.setFont(bold);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            setBorderThin(headerStyle);

            CellStyle dataStyle = wb.createCellStyle();
            setBorderThin(dataStyle);

            // Header row
            Row header = sheet.createRow(0);
            String[] headers = {"name", "description", "parentName"};
            for (int c = 0; c < headers.length; c++) {
                Cell cell = header.createCell(c);
                cell.setCellValue(headers[c]);
                cell.setCellStyle(headerStyle);
            }

            // Sample rows
            String[][] samples = {
                {"Торти",      "Святкові та весільні торти",        ""},
                {"Тістечка",   "Дрібна кондитерська продукція",     ""},
                {"Еклери",     "Заварні тістечка з кремом",          "Тістечка"},
                {"Макарони",   "Французькі макарон різних смаків",   "Тістечка"},
            };
            for (int r = 0; r < samples.length; r++) {
                Row row = sheet.createRow(r + 1);
                for (int c = 0; c < samples[r].length; c++) {
                    Cell cell = row.createCell(c);
                    cell.setCellValue(samples[r][c]);
                    cell.setCellStyle(dataStyle);
                }
            }

            // Auto-size columns
            for (int c = 0; c < headers.length; c++) {
                sheet.autoSizeColumn(c);
            }

            wb.write(out);
            return out.toByteArray();
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────────────────────────────────

    private String getCellString(Row row, int col) {
        Cell cell = row.getCell(col, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING  -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue()).trim();
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue()).trim();
            default      -> null;
        };
    }

    private void setBorderThin(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }
}
