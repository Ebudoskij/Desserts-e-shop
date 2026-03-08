package com.ebudoskij.dessert_shop.service;

import com.ebudoskij.dessert_shop.model.Media;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface MediaService {
    void saveEntityImages(String entityType, Long entityId, List<MultipartFile> images);
    void deleteEntityImages(List<Long> imageIds);
    List<Media> getEntityImages(String entityType, Long entityId);
}
