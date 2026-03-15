package com.ebudoskij.dessert_shop.service.impl;

import com.ebudoskij.dessert_shop.exception.EntityNotFoundException;
import com.ebudoskij.dessert_shop.mapper.AdditionalItemMapper;
import com.ebudoskij.dessert_shop.model.AdditionalItem;
import com.ebudoskij.dessert_shop.model.dto.PageResponseDto;
import com.ebudoskij.dessert_shop.model.dto.additionalItem.*;
import com.ebudoskij.dessert_shop.model.dto.media.MediaResponseDto;
import com.ebudoskij.dessert_shop.repository.AdditionalItemRepository;
import com.ebudoskij.dessert_shop.service.AdditionalItemService;
import com.ebudoskij.dessert_shop.service.MediaService;
import com.ebudoskij.dessert_shop.utils.specifications.AdditionalItemSpecificationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class AdditionalItemServiceImpl implements AdditionalItemService {

    private final AdditionalItemRepository additionalItemRepository;
    private final AdditionalItemMapper additionalItemMapper;
    private final MediaService mediaService;

    @Override
    public PageResponseDto<AdditionalItemCardDto> getAll(AdditionalItemFilterDto filter, Pageable pageable) {

        Specification<AdditionalItem> spec = AdditionalItemSpecificationUtil.buildFilters(filter);

        Page<AdditionalItemCardDto> additionalItemPage = additionalItemRepository.findAdditionalItemCards(spec, pageable);

        return new PageResponseDto<>(additionalItemPage);
    }

    @Override
    public AdditionalItemResponseDto getById(Long id) {
        AdditionalItem item = additionalItemRepository.findById(id)
                .filter(a -> !a.getIsDeleted())
                .orElseThrow(() -> new EntityNotFoundException("AdditionalItem not found with id: " + id));

        AdditionalItemResponseDto responseDto = additionalItemMapper.toDto(item);

        java.util.List<MediaResponseDto> mediaDtos = mediaService.getEntityImages("AdditionalItem", id).stream()
                .sorted(Comparator.comparing(MediaResponseDto::getPriority))
                .toList();
        responseDto.setImageUrls(mediaDtos);

        return responseDto;
    }

    @Override
    public void createAdditionalItem(AdditionalItemCreateDto dto) {
        AdditionalItem item = additionalItemMapper.toEntity(dto);
        item.setIsDeleted(false);
        AdditionalItem savedItem = additionalItemRepository.save(item);
        mediaService.saveEntityImages("AdditionalItem", savedItem.getId(), dto.getImages());
    }

    @Override
    public AdditionalItemResponseDto getToUpdate(Long id) {
        AdditionalItem item = additionalItemRepository.findById(id)
                .filter(a -> !a.getIsDeleted())
                .orElseThrow(() -> new EntityNotFoundException("AdditionalItem not found with id: " + id));

        AdditionalItemResponseDto responseDto = additionalItemMapper.toDto(item);
        
        java.util.List<com.ebudoskij.dessert_shop.model.dto.media.MediaResponseDto> mediaDtos = mediaService.getEntityImages("AdditionalItem", id).stream()
                .map(m -> {
                    com.ebudoskij.dessert_shop.model.dto.media.MediaResponseDto dto = new com.ebudoskij.dessert_shop.model.dto.media.MediaResponseDto();
                    dto.setId(m.getId());
                    dto.setUrl(m.getUrl());
                    return dto;
                })
                .toList();
        responseDto.setImageUrls(mediaDtos);

        return responseDto;
    }

    @Override
    public void updateById(Long id, AdditionalItemUpdateDto dto) {
        AdditionalItem existingItem = additionalItemRepository.findById(id)
                .filter(a -> !a.getIsDeleted())
                .orElseThrow(() -> new EntityNotFoundException("AdditionalItem not found with id: " + id));
        additionalItemMapper.updateEntityFromDto(dto, existingItem);
        AdditionalItem savedItem = additionalItemRepository.save(existingItem);
        
        mediaService.deleteEntityImages(dto.getDeletedImageIds());
        mediaService.saveEntityImages("AdditionalItem", savedItem.getId(), dto.getImages());
    }

    @Override
    public void deleteById(Long id) {
        AdditionalItem item = additionalItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("AdditionalItem not found with id: " + id));

        item.setIsDeleted(true);
        additionalItemRepository.save(item);
    }

    @Override
    public BigDecimal getMinPrice() {
        return additionalItemRepository.findMinPrice();
    }

    @Override
    public BigDecimal getMaxPrice() {
        return additionalItemRepository.findMaxPrice();
    }

    @Override
    public void restoreById(Long id) {
        AdditionalItem item = additionalItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("AdditionalItem not found with id: " + id));

        item.setIsDeleted(false);
        additionalItemRepository.save(item);
    }
}
