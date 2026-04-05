package com.ebudoskij.dessert_shop.service;

import com.ebudoskij.dessert_shop.model.Category;
import com.ebudoskij.dessert_shop.model.dto.category.CategoryCreateDto;
import jakarta.validation.Valid;

import java.util.List;

public interface CategoryService {
    List<Category> getAll();

    Category getById(Long id);

    void createCategory(@Valid CategoryCreateDto dto);

    void updateById(Long id, @Valid CategoryCreateDto dto);

    void deleteById(Long id);

    List<Long> getCategoryAndChildrenIds(Long categoryId);

    void restoreById(Long id);

    List<Category> getAllAdmin();
}
