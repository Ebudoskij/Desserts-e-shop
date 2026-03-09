package com.ebudoskij.dessert_shop.repository;

import com.ebudoskij.dessert_shop.model.Product;
import com.ebudoskij.dessert_shop.repository.custom.product.ProductRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>,
        JpaSpecificationExecutor<Product>,
        ProductRepositoryCustom {
}
