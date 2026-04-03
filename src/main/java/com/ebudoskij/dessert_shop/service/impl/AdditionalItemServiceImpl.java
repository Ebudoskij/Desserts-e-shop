package com.ebudoskij.dessert_shop.service.impl;

import com.ebudoskij.dessert_shop.audit.AuditLogHelper;
import com.ebudoskij.dessert_shop.audit.FieldDiffBuilder;
import com.ebudoskij.dessert_shop.exception.EntityNotFoundException;
import com.ebudoskij.dessert_shop.mapper.AdditionalItemMapper;
import com.ebudoskij.dessert_shop.model.AdditionalItem;
import com.ebudoskij.dessert_shop.model.dto.PageResponseDto;
import com.ebudoskij.dessert_shop.model.dto.additionalItem.*;
import com.ebudoskij.dessert_shop.model.dto.media.MediaResponseDto;
import com.ebudoskij.dessert_shop.model.enums.AuditActionType;
import com.ebudoskij.dessert_shop.repository.AdditionalItemRepository;
import com.ebudoskij.dessert_shop.service.AdditionalItemService;
import com.ebudoskij.dessert_shop.service.MediaService;
import com.ebudoskij.dessert_shop.utils.specifications.AdditionalItemSpecificationUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

    private static final String ENTITY_TYPE = "AdditionalItem";

    private final AdditionalItemRepository additionalItemRepository;
    private final AdditionalItemMapper additionalItemMapper;
    private final MediaService mediaService;
    private final AuditLogHelper auditLogHelper;
    private final ObjectMapper objectMapper;

    @Override
    public PageResponseDto<AdditionalItemCardDto> getAll(AdditionalItemFilterDto filter, Pageable pageable) {
        Specification<AdditionalItem> spec = AdditionalItemSpecificationUtil.buildFilters(filter);
        Page<AdditionalItemCardDto> page = additionalItemRepository.findAdditionalItemCards(spec, pageable);
        return new PageResponseDto<>(page);
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

        if (!mediaDtos.isEmpty()) {
            responseDto.setMainImageId(mediaDtos.getFirst().getId());
        }

        return responseDto;
    }

    @Override
    @Transactional
    public void createAdditionalItem(AdditionalItemCreateDto dto) {
        AdditionalItem item = additionalItemMapper.toEntity(dto);
        item.setIsDeleted(false);

        AdditionalItem saved = additionalItemRepository.save(item);

        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            mediaService.saveEntityImages("AdditionalItem", saved.getId(), dto.getImages(), dto.getMainImageIndex());
        }

        // ── Audit creation snapshot ──
        ObjectNode snapshot = objectMapper.createObjectNode();
        snapshot.put("name",       saved.getName());
        snapshot.put("extraPrice", saved.getExtraPrice() != null ? saved.getExtraPrice().toPlainString() : null);
        snapshot.put("isDeleted",  saved.getIsDeleted());

        auditLogHelper.log(ENTITY_TYPE, saved.getId(), AuditActionType.CREATED,
                snapshot,
                "Additional item '" + saved.getName() + "' was created");
    }

    @Override
    @Transactional
    public void updateById(Long id, AdditionalItemUpdateDto dto) {
        AdditionalItem existingItem = additionalItemRepository.findById(id)
                .filter(a -> !a.getIsDeleted())
                .orElseThrow(() -> new EntityNotFoundException("AdditionalItem not found with id: " + id));

        // ── Snapshot BEFORE changes ──
        String     oldName  = existingItem.getName();
        BigDecimal oldPrice = existingItem.getExtraPrice();

        additionalItemMapper.updateEntityFromDto(dto, existingItem);

        if (dto.getDeletedImageIds() != null && !dto.getDeletedImageIds().isEmpty()) {
            mediaService.deleteEntityImages(dto.getDeletedImageIds());
        }
        if (dto.getNewMainImageIndex() != null && dto.getImages() != null) {
            mediaService.saveEntityImages("AdditionalItem", id, dto.getImages(), dto.getNewMainImageIndex());
        } else {
            if (dto.getMainImageId() != null) {
                mediaService.setMainImageById("AdditionalItem", id, dto.getMainImageId());
            }
            if (dto.getImages() != null && !dto.getImages().isEmpty()) {
                mediaService.saveEntityImages("AdditionalItem", id, dto.getImages(), null);
            }
        }

        additionalItemRepository.save(existingItem);

        // ── Audit diff ──
        FieldDiffBuilder diff = new FieldDiffBuilder()
                .compare("name",       oldName,  existingItem.getName())
                .compare("extraPrice",
                        oldPrice != null ? oldPrice.toPlainString() : null,
                        existingItem.getExtraPrice() != null ? existingItem.getExtraPrice().toPlainString() : null);

        if (diff.hasChanges()) {
            auditLogHelper.log(ENTITY_TYPE, id, AuditActionType.UPDATED,
                    diff.build(objectMapper),
                    "Additional item '" + existingItem.getName() + "' was updated");
        }
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        AdditionalItem item = additionalItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("AdditionalItem not found with id: " + id));

        item.setIsDeleted(true);
        additionalItemRepository.save(item);

        auditLogHelper.log(ENTITY_TYPE, id, AuditActionType.DELETED,
                new FieldDiffBuilder().compare("isDeleted", false, true).build(objectMapper),
                "Additional item '" + item.getName() + "' was soft-deleted");
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
    @Transactional
    public void restoreById(Long id) {
        AdditionalItem item = additionalItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("AdditionalItem not found with id: " + id));

        item.setIsDeleted(false);
        additionalItemRepository.save(item);

        auditLogHelper.log(ENTITY_TYPE, id, AuditActionType.RESTORED,
                new FieldDiffBuilder().compare("isDeleted", true, false).build(objectMapper),
                "Additional item '" + item.getName() + "' was restored");
    }
}
