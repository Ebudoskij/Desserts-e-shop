package com.ebudoskij.dessert_shop.service.impl;

import com.ebudoskij.dessert_shop.exception.FileStorageException;
import com.ebudoskij.dessert_shop.mapper.MediaMapper;
import com.ebudoskij.dessert_shop.model.Media;
import com.ebudoskij.dessert_shop.model.dto.media.MediaResponseDto;
import com.ebudoskij.dessert_shop.repository.MediaRepository;
import com.ebudoskij.dessert_shop.service.MediaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaServiceImpl implements MediaService {

    private final MediaRepository mediaRepository;
    private final MediaMapper mediaMapper;

    @Value("${upload.path:uploads}")
    private String uploadPath;

    @Override
    @Transactional
    public void saveEntityImages(String entityType, Long entityId, List<MultipartFile> images, Integer newMainImageIndex) {
        if (images == null || images.isEmpty()) return;

        // If a new file is becoming 'Main', we demote whatever is currently 0 in the DB
        if (newMainImageIndex != null) {
            mediaRepository.demoteCurrentMain(entityType, entityId);
        }

        for (int i = 0; i < images.size(); i++) {
            MultipartFile file = images.get(i);
            if (file == null || file.isEmpty()) continue;

            // Priority 0 for the chosen one, others get a high offset to avoid clashes
            int priority = (newMainImageIndex != null && i == newMainImageIndex) ? 0 : (i + 10);
            saveImage(entityType, entityId, file, priority);
        }
    }

    @Override
    @Transactional
    public void setMainImageById(String entityType, Long entityId, Long mainImageId) {
        // Atomic swap: demotes the current 0 and promotes the target ID to 0 in one query
        mediaRepository.swapMainImage(entityType, entityId, mainImageId);
    }

    @Override
    @Transactional
    public void deleteEntityImages(List<Long> imageIds) {
        if (imageIds == null || imageIds.isEmpty()) return;

        List<Media> medias = mediaRepository.findAllById(imageIds);

        // Delete files from disk
        for (Media media : medias) {
            deletePhysicalFile(media.getUrl());
        }

        // Efficient batch delete from DB
        mediaRepository.deleteAllInBatch(medias);
    }

    @Override
    public List<MediaResponseDto> getEntityImages(String entityType, Long entityId) {
        return mediaRepository.findByEntityTypeAndEntityIdOrderByPriorityAsc(entityType, entityId).stream()
                .map(mediaMapper::toDto)
                .toList();
    }

    // --- Private Helper Methods ---

    private void saveImage(String entityType, Long entityId, MultipartFile file, int priority) {
        try {
            Path uploadDir = Paths.get(uploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = uploadDir.resolve(filename);
            Files.copy(file.getInputStream(), filePath);

            Media media = new Media();
            media.setEntityType(entityType);
            media.setEntityId(entityId);
            media.setUrl("/uploads/" + filename);
            media.setPriority(priority);
            mediaRepository.save(media);
        } catch (IOException e) {
            throw new FileStorageException("Storage error for file: " + file.getOriginalFilename());
        }
    }

    private void deletePhysicalFile(String url) {
        try {
            String filename = url.replace("/uploads/", "");
            Path filePath = Paths.get(uploadPath).resolve(filename);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // Log error, but don't fail the whole transaction
            log.error("Could not delete file at: {}", url);
        }
    }
}
