package com.ebudoskij.dessert_shop.repository;

import com.ebudoskij.dessert_shop.model.AdditionalItem;
import com.ebudoskij.dessert_shop.repository.custom.additionalItem.AdditionalItemRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface AdditionalItemRepository extends JpaRepository<AdditionalItem, Long>,
        JpaSpecificationExecutor<AdditionalItem>,
        AdditionalItemRepositoryCustom {

    @Query("SELECT MAX(a.extraPrice) FROM AdditionalItem a WHERE a.isDeleted = false")
    BigDecimal findMaxPrice();

    @Query("SELECT MIN(a.extraPrice) FROM AdditionalItem a WHERE a.isDeleted = false")
    BigDecimal findMinPrice();
}
