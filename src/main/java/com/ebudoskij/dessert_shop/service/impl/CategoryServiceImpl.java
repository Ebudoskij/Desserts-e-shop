package com.ebudoskij.dessert_shop.service.impl;

import com.ebudoskij.dessert_shop.audit.AuditLogHelper;
import com.ebudoskij.dessert_shop.audit.FieldDiffBuilder;
import com.ebudoskij.dessert_shop.exception.EntityNotFoundException;
import com.ebudoskij.dessert_shop.mapper.CategoryMapper;
import com.ebudoskij.dessert_shop.model.Category;
import com.ebudoskij.dessert_shop.model.dto.category.CategoryCreateDto;
import com.ebudoskij.dessert_shop.model.enums.AuditActionType;
import com.ebudoskij.dessert_shop.repository.CategoryRepository;
import com.ebudoskij.dessert_shop.service.CategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private static final String ENTITY_TYPE = "Category";

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final AuditLogHelper auditLogHelper;
    private final ObjectMapper objectMapper;

    @Override
    public List<Category> getAll() {
        return categoryRepository.findAll().stream()
                .filter(c -> !c.getIsDeleted())
                .toList();
    }

    @Override
    public List<Category> getAllAdmin() {
        return categoryRepository.findAll().stream()
                .toList();
    }

    @Override
    public Category getById(Long id) {
        return categoryRepository.findById(id)
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
        Category saved = categoryRepository.save(category);

        // ── Audit creation snapshot ──
        ObjectNode snapshot = objectMapper.createObjectNode();
        snapshot.put("name",      saved.getName());
        snapshot.put("parent",    saved.getParent() != null ? saved.getParent().getName() : null);
        snapshot.put("isDeleted", saved.getIsDeleted());

        auditLogHelper.log(ENTITY_TYPE, saved.getId(), AuditActionType.CREATED,
                snapshot,
                "Category '" + saved.getName() + "' was created");
    }

    @Override
    public void updateById(Long id, CategoryCreateDto dto) {
        Category existingCategory = getById(id);

        // ── Snapshot BEFORE changes ──
        String oldName       = existingCategory.getName();
        String oldParentName = existingCategory.getParent() != null
                ? existingCategory.getParent().getName() : null;

        categoryMapper.updateEntityFromDto(dto, existingCategory);

        if (dto.getParentId() != null) {
            Category parent = getById(dto.getParentId());
            existingCategory.setParent(parent);
        } else {
            existingCategory.setParent(null);
        }

        categoryRepository.save(existingCategory);

        // ── Audit diff ──
        String newParentName = existingCategory.getParent() != null
                ? existingCategory.getParent().getName() : null;
        FieldDiffBuilder diff = new FieldDiffBuilder()
                .compare("name",   oldName,       existingCategory.getName())
                .compare("parent", oldParentName, newParentName);

        if (diff.hasChanges()) {
            auditLogHelper.log(ENTITY_TYPE, id, AuditActionType.UPDATED,
                    diff.build(objectMapper),
                    "Category '" + existingCategory.getName() + "' was updated");
        }
    }

    @Override
    public void deleteById(Long id) {
        Category category = getById(id);
        category.setIsDeleted(true);
        categoryRepository.save(category);

        auditLogHelper.log(ENTITY_TYPE, id, AuditActionType.DELETED,
                new FieldDiffBuilder().compare("isDeleted", false, true).build(objectMapper),
                "Category '" + category.getName() + "' was soft-deleted");
    }

    @Override
    public List<Long> getCategoryAndChildrenIds(Long categoryId) {
        List<Long> result = new ArrayList<>();
        collect(categoryId, result);
        return result;
    }

    @Override
    public void restoreById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + id));

        category.setIsDeleted(false);
        categoryRepository.save(category);

        auditLogHelper.log(ENTITY_TYPE, id, AuditActionType.RESTORED,
                new FieldDiffBuilder().compare("isDeleted", true, false).build(objectMapper),
                "Category '" + category.getName() + "' was restored");
    }

    private void collect(Long categoryId, List<Long> result) {
        result.add(categoryId);
        List<Category> children = categoryRepository.findByParentId(categoryId);
        for (Category child : children) {
            collect(child.getId(), result);
        }
    }
}
