package com.ebudoskij.dessert_shop.service;

import com.ebudoskij.dessert_shop.model.Product;
import com.ebudoskij.dessert_shop.model.dto.PageResponseDto;
import com.ebudoskij.dessert_shop.model.dto.product.ProductCreateDto;
import jakarta.validation.Valid;

public interface ProductService {
    PageResponseDto<Product> getAll(int page, int size, String sortBy, String sortDir, String searchQuery);

    Product getById(Long id);

    void updateById(Long id, @Valid ProductCreateDto dto);

    void deleteById(Long id);

    void createProduct(@Valid ProductCreateDto dto);
}
