package com.ebudoskij.dessert_shop.service;

import com.ebudoskij.dessert_shop.model.dto.PageResponseDto;
import com.ebudoskij.dessert_shop.model.dto.product.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface ProductService {
    PageResponseDto<ProductCardDto> getAll(ProductFilteringDto filter, Pageable pageable);

    ProductResponseDto getById(Long id);

    void updateById(Long id, @Valid ProductUpdateDto dto);

    void deleteById(Long id);

    void createProduct(@Valid ProductCreateDto dto);

    BigDecimal getMaxPrice();

    BigDecimal getMinPrice();

    void restoreById(Long id);
}
