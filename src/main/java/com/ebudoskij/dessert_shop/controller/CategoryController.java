package com.ebudoskij.dessert_shop.controller;

import com.ebudoskij.dessert_shop.model.Category;
import com.ebudoskij.dessert_shop.model.dto.ImportResult;
import com.ebudoskij.dessert_shop.model.dto.category.CategoryCreateDto;
import com.ebudoskij.dessert_shop.model.dto.category.CategoryUpdateDto;
import com.ebudoskij.dessert_shop.model.enums.UnitType;
import com.ebudoskij.dessert_shop.service.CategoryImportService;
import com.ebudoskij.dessert_shop.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;
    private final CategoryImportService categoryImportService;

    @GetMapping
    public String fetchAll(Model model){
        List<Category> response = categoryService.getAllAdmin();

        model.addAttribute("response", response);

        return "category/categories";
    }

    @GetMapping("/{id}")
    public String fetchById(@PathVariable Long id,
                            Model model){
        Category response = categoryService.getById(id);

        model.addAttribute("response", response);

        return "category/category";
    }

    @GetMapping("/add")
    public String createCategoryPage(Model model){
        model.addAttribute("category", new CategoryCreateDto());
        model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("unitTypes", UnitType.values());

        return "category/newCategory";
    }

    @PostMapping
    public String createCategory(@ModelAttribute("category") @Valid CategoryCreateDto dto,
                                BindingResult bindingResult,
                                Model model){
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.getAll());
            model.addAttribute("unitTypes", UnitType.values());

            return "category/newCategory";
        }

        categoryService.createCategory(dto);
        return "redirect:/categories";
    }

    @GetMapping("/{id}/update")
    public String updateCategoryPage(@PathVariable Long id, Model model){

        model.addAttribute("category", new CategoryUpdateDto(categoryService.getById(id)));
        model.addAttribute("categories", categoryService.getAll());

        return "category/updateCategory";
    }

    @PutMapping("/{id}")
    public String updateById(@PathVariable Long id,
                             @ModelAttribute("category") @Valid CategoryCreateDto dto,
                             BindingResult bindingResult,
                             Model model){
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.getAll());
            return "category/updateCategory";
        }

        categoryService.updateById(id, dto);

        return "redirect:/categories";
    }

    @DeleteMapping("/{id}")
    public String deleteById(@PathVariable Long id){
        categoryService.deleteById(id);
        return "redirect:/categories";
    }

    @PostMapping("/{id}/restore")
    public String restoreById(@PathVariable Long id){
        categoryService.restoreById(id);
        return "redirect:/categories";
    }

    // ── XLSX Import ──────────────────────────────────────────────────────────

    @PostMapping("/import")
    public String importFromExcel(@RequestParam("file") MultipartFile file,
                                  RedirectAttributes ra) {
        // Validate: not empty
        if (file == null || file.isEmpty()) {
            ra.addFlashAttribute("importError", "Файл не обрано або він порожній.");
            return "redirect:/categories";
        }

        // Validate: .xlsx extension
        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.toLowerCase().endsWith(".xlsx")) {
            ra.addFlashAttribute("importError", "Дозволено лише файли формату .xlsx.");
            return "redirect:/categories";
        }

        try {
            ImportResult result = categoryImportService.importFromXlsx(file);
            if (result.success()) {
                ra.addFlashAttribute("importSuccess",
                        "Імпортовано " + result.imported() + " категорій успішно.");
            } else {
                String errorMsg = String.join(" | ", result.errors());
                ra.addFlashAttribute("importError", errorMsg);
            }
        } catch (IOException e) {
            ra.addFlashAttribute("importError",
                    "Помилка читання файлу: " + e.getMessage());
        }

        return "redirect:/categories";
    }

    @GetMapping("/import/template")
    public ResponseEntity<byte[]> downloadTemplate() {
        try {
            byte[] bytes = categoryImportService.buildTemplate();
            return ResponseEntity.ok()
                    .header("Content-Disposition",
                            "attachment; filename=\"bakery_categories_template.xlsx\"")
                    .header("Content-Type",
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    .body(bytes);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
