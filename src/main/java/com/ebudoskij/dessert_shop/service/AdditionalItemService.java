package com.ebudoskij.dessert_shop.service;

import com.ebudoskij.dessert_shop.model.AdditionalItem;
import com.ebudoskij.dessert_shop.model.dto.PageResponseDto;
import com.ebudoskij.dessert_shop.model.dto.additionalItem.AdditionalItemCreateDto;
import com.ebudoskij.dessert_shop.model.dto.additionalItem.AdditionalItemResponseDto;
import com.ebudoskij.dessert_shop.model.dto.additionalItem.AdditionalItemUpdateDto;
import jakarta.validation.Valid;

public interface AdditionalItemService {
    PageResponseDto<AdditionalItem> getAll(int page, int size, String sortBy, String sortDir, String searchQuery);

    AdditionalItem getById(Long id);

    void createAdditionalItem(@Valid AdditionalItemCreateDto dto);

    AdditionalItemResponseDto getToUpdate(Long id);

    void updateById(Long id, @Valid AdditionalItemUpdateDto dto);

    void deleteById(Long id);
}
