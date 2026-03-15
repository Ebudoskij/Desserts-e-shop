package com.ebudoskij.dessert_shop.model.dto.additionalItem;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class AdditionalItemUpdateDto {
    @NotBlank
    private String name;

    private String description;

    @Positive
    private BigDecimal extraPrice;

    private List<MultipartFile> images;

    private List<Long> deletedImageIds;

    // If this is set, an existing image becomes main
    private Long mainImageId;

    // If the user picks a BRAND NEW upload as main,
    // we use the index of the 'images' list
    private Integer newMainImageIndex;
}
