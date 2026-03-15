package com.ebudoskij.dessert_shop.service.impl;

import com.ebudoskij.dessert_shop.exception.EntityNotFoundException;
import com.ebudoskij.dessert_shop.mapper.ProductMapper;
import com.ebudoskij.dessert_shop.model.Category;
import com.ebudoskij.dessert_shop.model.Product;
import com.ebudoskij.dessert_shop.model.dto.PageResponseDto;
import com.ebudoskij.dessert_shop.model.dto.product.*;
import com.ebudoskij.dessert_shop.model.dto.media.MediaResponseDto;
import com.ebudoskij.dessert_shop.repository.ProductRepository;
import com.ebudoskij.dessert_shop.service.CategoryService;
import com.ebudoskij.dessert_shop.service.MediaService;
import com.ebudoskij.dessert_shop.service.ProductService;
import com.ebudoskij.dessert_shop.utils.specifications.ProductSpecificationsUtil;
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
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final CategoryService categoryService;
    private final MediaService mediaService;
    private final ProductSpecificationsUtil productSpecificationsUtil;

    @Override
    public PageResponseDto<ProductCardDto> getAll(ProductFilteringDto filter, Pageable pageable) {
        Specification<Product> spec = productSpecificationsUtil.buildFilters(filter);

        Page<ProductCardDto> productPage = productRepository.findProductCards(spec, pageable);

        return new PageResponseDto<>(productPage);
    }

    @Override
    public ProductResponseDto getById(Long id) {
        Product product = productRepository.findById(id)
                .filter(p -> !p.getIsDeleted())
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));

        ProductResponseDto responseDto = productMapper.toDto(product);

        List<MediaResponseDto> mediaDtos = mediaService.getEntityImages("Product", id).stream()
                .sorted(Comparator.comparing(MediaResponseDto::getPriority))
                .toList();
        responseDto.setImages(mediaDtos);

        if (!mediaDtos.isEmpty()){
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

        productMapper.updateEntityFromDto(dto, existingProduct);

        // 1. Handle Category
        if (dto.getCategoryId() != null) {
            existingProduct.setCategory(categoryService.getById(dto.getCategoryId()));
        }

        // 2. Delete images first
        if (dto.getDeletedImageIds() != null && !dto.getDeletedImageIds().isEmpty()) {
            mediaService.deleteEntityImages(dto.getDeletedImageIds());
        }

        // 3. Handle Main Image Logic (Mutual Exclusion)
        // Priority: New uploads usually take precedence if both are sent,
        // but typically the UI should only allow one selection.
        if (dto.getNewMainImageIndex() != null && dto.getImages() != null) {
            // If a NEW image is main, we don't care about mainImageId.
            // saveEntityImages already calls demoteCurrentMain inside.
            mediaService.saveEntityImages("Product", id, dto.getImages(), dto.getNewMainImageIndex());
        } else {
            // If no NEW image is main, check if we need to swap to an existing one
            if (dto.getMainImageId() != null) {
                mediaService.setMainImageById("Product", id, dto.getMainImageId());
            }
            // Save remaining new images normally (index null means no new main)
            if (dto.getImages() != null && !dto.getImages().isEmpty()) {
                mediaService.saveEntityImages("Product", id, dto.getImages(), null);
            }
        }

        productRepository.save(existingProduct);
    }

    @Override
    public void deleteById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));

        product.setIsDeleted(true);
        productRepository.save(product);
    }

    @Override
    @Transactional
    public void createProduct(ProductCreateDto dto) {
        // 1. Map DTO to Entity
        Product product = productMapper.toEntity(dto);

        // Explicitly set default state (though your DB or Entity might do this too)
        product.setIsDeleted(false);

        // 2. Handle Category association
        if (dto.getCategoryId() != null) {
            Category category = categoryService.getById(dto.getCategoryId());
            product.setCategory(category);
        }

        // 3. Save the product first to generate the ID needed for Media
        Product savedProduct = productRepository.save(product);

        // 4. Save images using the updated MediaService
        // We pass the newMainImageIndex so the service can assign priority 0
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            mediaService.saveEntityImages(
                    "Product",
                    savedProduct.getId(),
                    dto.getImages(),
                    dto.getMainImageIndex() // Ensure this field exists in ProductCreateDto
            );
        }
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
    public void restoreById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));

        product.setIsDeleted(false);
        productRepository.save(product);
    }
}
