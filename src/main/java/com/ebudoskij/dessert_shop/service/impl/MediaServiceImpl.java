package com.ebudoskij.dessert_shop.service.impl;

import com.ebudoskij.dessert_shop.mapper.MediaMapper;
import com.ebudoskij.dessert_shop.model.Media;
import com.ebudoskij.dessert_shop.model.dto.media.MediaResponseDto;
import com.ebudoskij.dessert_shop.repository.MediaRepository;
import com.ebudoskij.dessert_shop.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {

    private final MediaRepository mediaRepository;
    private final MediaMapper mediaMapper;

    @Value("${upload.path:uploads}")
    private String uploadPath;

    @Override
    public void saveEntityImages(String entityType, Long entityId, List<MultipartFile> images) {
        if (images == null || images.isEmpty()) return;

        try {
            Path uploadDir = Paths.get(uploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            for (int i = 0; i < images.size(); i++) {
                MultipartFile file = images.get(i);
                if (file.isEmpty()) continue;

                String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
                Path filePath = uploadDir.resolve(filename);
                Files.copy(file.getInputStream(), filePath);

                Media media = new Media();
                media.setEntityType(entityType);
                media.setEntityId(entityId);
                media.setUrl("/uploads/" + filename);
                media.setPriority(i);
                mediaRepository.save(media);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not store files", e);
        }
    }

    @Override
    public void deleteEntityImages(List<Long> imageIds) {
        if (imageIds == null || imageIds.isEmpty()) return;

        for (Long id : imageIds) {
            mediaRepository.findById(id).ifPresent(media -> {
                try {
                    String filename = media.getUrl().replace("/uploads/", "");
                    Path filePath = Paths.get(uploadPath).resolve(filename);
                    Files.deleteIfExists(filePath);
                } catch (IOException e) {
                    System.err.println("Could not delete file: " + media.getUrl());
                }
                mediaRepository.delete(media);
            });
        }
    }

    @Override
    public List<MediaResponseDto> getEntityImages(String entityType, Long entityId) {
        return mediaRepository.findByEntityTypeAndEntityIdOrderByPriorityAsc(entityType, entityId).stream()
                .map(mediaMapper::toDto)
                .toList();
    }
}
