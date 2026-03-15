package com.ebudoskij.dessert_shop.service;

import com.ebudoskij.dessert_shop.model.dto.PageResponseDto;
import com.ebudoskij.dessert_shop.model.dto.additionalItem.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface AdditionalItemService {
    PageResponseDto<AdditionalItemCardDto> getAll(AdditionalItemFilterDto filter, Pageable pageable);

    AdditionalItemResponseDto getById(Long id);

    void createAdditionalItem(@Valid AdditionalItemCreateDto dto);

    void updateById(Long id, @Valid AdditionalItemUpdateDto dto);

    void deleteById(Long id);

    BigDecimal getMinPrice();

    BigDecimal getMaxPrice();

    void restoreById(Long id);
}
