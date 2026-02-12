package com.ebudoskij.dessert_shop.repository;

import com.ebudoskij.dessert_shop.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
