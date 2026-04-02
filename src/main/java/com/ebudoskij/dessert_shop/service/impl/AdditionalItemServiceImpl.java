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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

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
                .orElseThrow(() -> new EntityNotFoundException("AdditionalItem not found with id: " + id));

        AdditionalItemResponseDto responseDto = additionalItemMapper.toDto(item);

        List<MediaResponseDto> mediaDtos = mediaService.getEntityImages("AdditionalItem", id).stream()
                .sorted(Comparator.comparing(MediaResponseDto::getPriority))
                .toList();
        responseDto.setImageUrls(mediaDtos);

        if (!mediaDtos.isEmpty()){
            responseDto.setMainImageId(mediaDtos.getFirst().getId());
        }

        return responseDto;
    }

    @Override
    @Transactional
    public void createAdditionalItem(AdditionalItemCreateDto dto) {
        // 1. Map and initialize basic state
        AdditionalItem item = additionalItemMapper.toEntity(dto);
        item.setIsDeleted(false);

        // 2. Persist to generate the ID
        AdditionalItem savedItem = additionalItemRepository.save(item);

        // 3. Save images with index awareness
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            mediaService.saveEntityImages(
                    "AdditionalItem",
                    savedItem.getId(),
                    dto.getImages(),
                    dto.getMainImageIndex() // Ensure this is in your AdditionalItemCreateDto
            );
        }
    }

    @Override
    @Transactional
    public void updateById(Long id, AdditionalItemUpdateDto dto) {
        // 1. Fetch and validate existence
        AdditionalItem existingItem = additionalItemRepository.findById(id)
                .filter(a -> !a.getIsDeleted())
                .orElseThrow(() -> new EntityNotFoundException("AdditionalItem not found with id: " + id));

        // 2. Update basic fields (name, price, etc.)
        additionalItemMapper.updateEntityFromDto(dto, existingItem);

        // 3. Delete requested images first to free up priority slots/space
        if (dto.getDeletedImageIds() != null && !dto.getDeletedImageIds().isEmpty()) {
            mediaService.deleteEntityImages(dto.getDeletedImageIds());
        }

        // 4. Handle Main Image Logic (Mutual Exclusion)
        // Priority 1: Check if a NEW upload is designated as main
        if (dto.getNewMainImageIndex() != null && dto.getImages() != null) {
            mediaService.saveEntityImages("AdditionalItem", id, dto.getImages(), dto.getNewMainImageIndex());
        } else {
            // Priority 2: If no new main, check if an EXISTING image was selected as main
            if (dto.getMainImageId() != null) {
                mediaService.setMainImageById("AdditionalItem", id, dto.getMainImageId());
            }

            // Save any remaining new images without making them main
            if (dto.getImages() != null && !dto.getImages().isEmpty()) {
                mediaService.saveEntityImages("AdditionalItem", id, dto.getImages(), null);
            }
        }

        // 5. Save the item (Hibernate handles dirty checking, but explicit save is fine)
        additionalItemRepository.save(existingItem);
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
