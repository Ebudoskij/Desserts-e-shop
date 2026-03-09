package com.ebudoskij.dessert_shop.repository.custom.additionalItem;

import com.ebudoskij.dessert_shop.model.AdditionalItem;
import com.ebudoskij.dessert_shop.model.dto.additionalItem.AdditionalItemCardDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface AdditionalItemRepositoryCustom {
    Page<AdditionalItemCardDto> findAdditionalItemCards(Specification<AdditionalItem> spec, Pageable pageable);
}