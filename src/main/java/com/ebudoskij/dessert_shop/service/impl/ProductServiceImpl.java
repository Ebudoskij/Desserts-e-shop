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

import java.math.BigDecimal;
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
                .map(m -> {
                    MediaResponseDto mediaDto = new MediaResponseDto();
                    mediaDto.setId(m.getId());
                    mediaDto.setUrl(m.getUrl());
                    return mediaDto;
                })
                .toList();
        responseDto.setImages(mediaDtos);

        return responseDto;
    }

    @Override
    public void updateById(Long id, ProductUpdateDto dto) {
        Product existingProduct = productRepository.findById(id)
                        .filter(p -> !p.getIsDeleted())
                        .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));

        productMapper.updateEntityFromDto(dto, existingProduct);

        if (dto.getCategoryId() != null) {
            Category category = categoryService.getById(dto.getCategoryId());
            existingProduct.setCategory(category);
        } else {
            existingProduct.setCategory(null);
        }

        Product savedProduct = productRepository.save(existingProduct);
        mediaService.deleteEntityImages(dto.getDeletedImageIds());
        mediaService.saveEntityImages("Product", savedProduct.getId(), dto.getImages());
    }

    @Override
    public void deleteById(Long id) {
        Product product = productRepository.findById(id)
                .filter(p -> !p.getIsDeleted())
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));

        product.setIsDeleted(true);
        productRepository.save(product);
    }

    @Override
    public void createProduct(ProductCreateDto dto) {
        Product product = productMapper.toEntity(dto);
        product.setIsDeleted(false);

        if (dto.getCategoryId() != null) {
            Category category = categoryService.getById(dto.getCategoryId());
            product.setCategory(category);
        }

        Product savedProduct = productRepository.save(product);
        mediaService.saveEntityImages("Product", savedProduct.getId(), dto.getImages());
    }

    @Override
    public ProductResponseDto getToUpdate(Long id) {
        Product product = productRepository.findById(id)
                .filter(p -> !p.getIsDeleted())
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));

        ProductResponseDto responseDto = productMapper.toDto(product);
        
        List<MediaResponseDto> mediaDtos = mediaService.getEntityImages("Product", id).stream()
                .map(m -> {
                    MediaResponseDto mediaDto = new MediaResponseDto();
                    mediaDto.setId(m.getId());
                    mediaDto.setUrl(m.getUrl());
                    return mediaDto;
                })
                .toList();
        responseDto.setImages(mediaDtos);

        return responseDto;
    }

    @Override
    public BigDecimal getMaxPrice() {
        return productRepository.findMaxPrice();
    }

    @Override
    public BigDecimal getMinPrice() {
        return productRepository.findMinPrice();
    }
}
