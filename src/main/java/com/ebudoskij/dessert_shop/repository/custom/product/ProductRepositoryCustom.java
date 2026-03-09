package com.ebudoskij.dessert_shop.repository.custom.product;

import com.ebudoskij.dessert_shop.model.Product;
import com.ebudoskij.dessert_shop.model.dto.product.ProductCardDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface ProductRepositoryCustom {
    Page<ProductCardDto> findProductCards(Specification<Product> specification, Pageable pageable);
}
