package com.ebudoskij.dessert_shop.service.impl;

import com.ebudoskij.dessert_shop.exception.EntityNotFoundException;
import com.ebudoskij.dessert_shop.mapper.CategoryMapper;
import com.ebudoskij.dessert_shop.model.Category;
import com.ebudoskij.dessert_shop.model.dto.category.CategoryCreateDto;
import com.ebudoskij.dessert_shop.repository.CategoryRepository;
import com.ebudoskij.dessert_shop.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public List<Category> getAll() {
        return categoryRepository.findAll().stream()
                .filter(c -> !c.getIsDeleted())
                .toList();
    }

    @Override
    public Category getById(Long id) {
        return categoryRepository.findById(id)
                .filter(c -> !c.getIsDeleted())
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + id));
    }

    @Override
    public void createCategory(CategoryCreateDto dto) {
        Category category = categoryMapper.toEntity(dto);
        category.setIsDeleted(false);
        if (dto.getParentId() != null) {
            Category parent = getById(dto.getParentId());
            category.setParent(parent);
        }
        categoryRepository.save(category);
    }

    @Override
    public void updateById(Long id, CategoryCreateDto dto) {
        Category existingCategory = getById(id);
        categoryMapper.updateEntityFromDto(dto, existingCategory);
        
        if (dto.getParentId() != null) {
            Category parent = getById(dto.getParentId());
            existingCategory.setParent(parent);
        } else {
            existingCategory.setParent(null);
        }
        
        categoryRepository.save(existingCategory);
    }

    @Override
    public void deleteById(Long id) {
        Category category = getById(id);
        category.setIsDeleted(true);
        categoryRepository.save(category);
    }
}
