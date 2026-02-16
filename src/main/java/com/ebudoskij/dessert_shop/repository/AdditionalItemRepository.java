package com.ebudoskij.dessert_shop.repository;

import com.ebudoskij.dessert_shop.model.AdditionalItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdditionalItemRepository extends JpaRepository<AdditionalItem, Long> {
}
