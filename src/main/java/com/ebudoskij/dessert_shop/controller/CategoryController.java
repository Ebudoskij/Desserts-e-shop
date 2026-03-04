package com.ebudoskij.dessert_shop.controller;

import com.ebudoskij.dessert_shop.model.Category;
import com.ebudoskij.dessert_shop.model.dto.category.CategoryCreateDto;
import com.ebudoskij.dessert_shop.model.enums.UnitType;
import com.ebudoskij.dessert_shop.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/categories")
public class CategoryController {
    CategoryService categoryService;

    @GetMapping
    public String fetchAll(Model model){
        List<Category> response = categoryService.getAll();

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



    @PutMapping("/{id}")
    public String updateById(@PathVariable Long id,
                             @ModelAttribute("category") @Valid CategoryCreateDto dto,
                             BindingResult bindingResult,
                             Model model){
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.getAll());
            model.addAttribute("unitTypes", UnitType.values());
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
}
