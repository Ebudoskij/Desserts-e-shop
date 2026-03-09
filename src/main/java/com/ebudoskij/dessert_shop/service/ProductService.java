package com.ebudoskij.dessert_shop.service;

import com.ebudoskij.dessert_shop.model.dto.PageResponseDto;
import com.ebudoskij.dessert_shop.model.dto.product.ProductCardDto;
import com.ebudoskij.dessert_shop.model.dto.product.ProductCreateDto;
import com.ebudoskij.dessert_shop.model.dto.product.ProductResponseDto;
import com.ebudoskij.dessert_shop.model.dto.product.ProductUpdateDto;
import jakarta.validation.Valid;

public interface ProductService {
    PageResponseDto<ProductCardDto> getAll(
            int page,
            int size,
            String sortBy,
            String sortDir,
            String searchQuery,
            Boolean deleted
    );

    ProductResponseDto getById(Long id);

    void updateById(Long id, @Valid ProductUpdateDto dto);

    void deleteById(Long id);

    void createProduct(@Valid ProductCreateDto dto);

    ProductResponseDto getToUpdate(Long id);
}
