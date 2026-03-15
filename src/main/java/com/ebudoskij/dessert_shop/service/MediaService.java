package com.ebudoskij.dessert_shop.service;

import com.ebudoskij.dessert_shop.model.dto.media.MediaResponseDto;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface MediaService {
    void saveEntityImages(String entityType, Long entityId, List<MultipartFile> images, Integer newMainImageIndex);
    void setMainImageById(String entityType, Long entityId, Long mainImageId);
    void deleteEntityImages(List<Long> imageIds);
    List<MediaResponseDto> getEntityImages(String entityType, Long entityId);

}
