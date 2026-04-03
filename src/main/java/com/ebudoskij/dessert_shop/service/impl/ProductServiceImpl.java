package com.ebudoskij.dessert_shop.service.impl;

import com.ebudoskij.dessert_shop.audit.AuditLogHelper;
import com.ebudoskij.dessert_shop.audit.FieldDiffBuilder;
import com.ebudoskij.dessert_shop.exception.EntityNotFoundException;
import com.ebudoskij.dessert_shop.mapper.ProductMapper;
import com.ebudoskij.dessert_shop.model.Category;
import com.ebudoskij.dessert_shop.model.Product;
import com.ebudoskij.dessert_shop.model.dto.PageResponseDto;
import com.ebudoskij.dessert_shop.model.dto.product.*;
import com.ebudoskij.dessert_shop.model.dto.media.MediaResponseDto;
import com.ebudoskij.dessert_shop.model.enums.AuditActionType;
import com.ebudoskij.dessert_shop.model.enums.UnitType;
import com.ebudoskij.dessert_shop.repository.ProductRepository;
import com.ebudoskij.dessert_shop.service.CategoryService;
import com.ebudoskij.dessert_shop.service.MediaService;
import com.ebudoskij.dessert_shop.service.ProductService;
import com.ebudoskij.dessert_shop.utils.specifications.ProductSpecificationsUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private static final String ENTITY_TYPE = "Product";

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final CategoryService categoryService;
    private final MediaService mediaService;
    private final ProductSpecificationsUtil productSpecificationsUtil;
    private final AuditLogHelper auditLogHelper;
    private final ObjectMapper objectMapper;

    @Override
    public PageResponseDto<ProductCardDto> getAll(ProductFilteringDto filter, Pageable pageable) {
        Specification<Product> spec = productSpecificationsUtil.buildFilters(filter);
        Page<ProductCardDto> productPage = productRepository.findProductCards(spec, pageable);
        return new PageResponseDto<>(productPage);
    }

    @Override
    public ProductResponseDto getById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));

        ProductResponseDto responseDto = productMapper.toDto(product);

        List<MediaResponseDto> mediaDtos = mediaService.getEntityImages("Product", id).stream()
                .sorted(Comparator.comparing(MediaResponseDto::getPriority))
                .toList();
        responseDto.setImages(mediaDtos);

        if (!mediaDtos.isEmpty()) {
            responseDto.setMainImageId(mediaDtos.getFirst().getId());
        }

        return responseDto;
    }

    @Override
    @Transactional
    public void updateById(Long id, ProductUpdateDto dto) {
        Product existingProduct = productRepository.findById(id)
                .filter(p -> !p.getIsDeleted())
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));

        // ── Snapshot BEFORE changes ──
        String oldName         = existingProduct.getName();
        String oldCategoryName = existingProduct.getCategory() != null
                ? existingProduct.getCategory().getName() : null;
        BigDecimal oldPrice    = existingProduct.getPricePerUnit();
        UnitType   oldUnitType = existingProduct.getUnitType();
        Boolean    oldCustom   = existingProduct.getCustomizable();

        // ── Apply update ──
        productMapper.updateEntityFromDto(dto, existingProduct);

        if (dto.getCategoryId() != null) {
            existingProduct.setCategory(categoryService.getById(dto.getCategoryId()));
        }
        if (dto.getDeletedImageIds() != null && !dto.getDeletedImageIds().isEmpty()) {
            mediaService.deleteEntityImages(dto.getDeletedImageIds());
        }
        if (dto.getNewMainImageIndex() != null && dto.getImages() != null) {
            mediaService.saveEntityImages("Product", id, dto.getImages(), dto.getNewMainImageIndex());
        } else {
            if (dto.getMainImageId() != null) {
                mediaService.setMainImageById("Product", id, dto.getMainImageId());
            }
            if (dto.getImages() != null && !dto.getImages().isEmpty()) {
                mediaService.saveEntityImages("Product", id, dto.getImages(), null);
            }
        }

        productRepository.save(existingProduct);

        // ── Audit diff ──
        String newCategoryName = existingProduct.getCategory() != null
                ? existingProduct.getCategory().getName() : null;
        FieldDiffBuilder diff = new FieldDiffBuilder()
                .compare("name",         oldName,         existingProduct.getName())
                .compare("category",     oldCategoryName, newCategoryName)
                .compare("pricePerUnit", oldPrice != null ? oldPrice.toPlainString() : null,
                        existingProduct.getPricePerUnit() != null ? existingProduct.getPricePerUnit().toPlainString() : null)
                .compare("unitType",     oldUnitType != null ? oldUnitType.name() : null,
                        existingProduct.getUnitType() != null ? existingProduct.getUnitType().name() : null)
                .compare("customizable", oldCustom, existingProduct.getCustomizable());

        if (diff.hasChanges()) {
            auditLogHelper.log(ENTITY_TYPE, id, AuditActionType.UPDATED,
                    diff.build(objectMapper),
                    "Product '" + existingProduct.getName() + "' was updated");
        }
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));

        product.setIsDeleted(true);
        productRepository.save(product);

        auditLogHelper.log(ENTITY_TYPE, id, AuditActionType.DELETED,
                new FieldDiffBuilder().compare("isDeleted", false, true).build(objectMapper),
                "Product '" + product.getName() + "' was soft-deleted");
    }

    @Override
    @Transactional
    public void createProduct(ProductCreateDto dto) {
        Product product = productMapper.toEntity(dto);
        product.setIsDeleted(false);

        if (dto.getCategoryId() != null) {
            Category category = categoryService.getById(dto.getCategoryId());
            product.setCategory(category);
        }

        Product saved = productRepository.save(product);

        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            mediaService.saveEntityImages("Product", saved.getId(), dto.getImages(), dto.getMainImageIndex());
        }

        // ── Audit creation snapshot ──
        ObjectNode snapshot = objectMapper.createObjectNode();
        snapshot.put("name",         saved.getName());
        snapshot.put("category",     saved.getCategory() != null ? saved.getCategory().getName() : null);
        snapshot.put("pricePerUnit", saved.getPricePerUnit() != null ? saved.getPricePerUnit().toPlainString() : null);
        snapshot.put("unitType",     saved.getUnitType() != null ? saved.getUnitType().name() : null);
        snapshot.put("customizable", saved.getCustomizable());
        snapshot.put("isDeleted",    saved.getIsDeleted());

        auditLogHelper.log(ENTITY_TYPE, saved.getId(), AuditActionType.CREATED,
                snapshot,
                "Product '" + saved.getName() + "' was created");
    }

    @Override
    public BigDecimal getMaxPrice() {
        return productRepository.findMaxPrice();
    }

    @Override
    public BigDecimal getMinPrice() {
        return productRepository.findMinPrice();
    }

    @Override
    @Transactional
    public void restoreById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));

        product.setIsDeleted(false);
        productRepository.save(product);

        auditLogHelper.log(ENTITY_TYPE, id, AuditActionType.RESTORED,
                new FieldDiffBuilder().compare("isDeleted", true, false).build(objectMapper),
                "Product '" + product.getName() + "' was restored");
    }
}
