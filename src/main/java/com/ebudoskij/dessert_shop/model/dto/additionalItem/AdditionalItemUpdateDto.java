package com.ebudoskij.dessert_shop.model.dto.additionalItem;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class AdditionalItemUpdateDto {
    @NotBlank(message = "Name is required")
    @Size(max = 200, message = "Name must not exceed 200 characters")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @NotNull(message = "Extra price is required")
    @Positive(message = "Extra price must be a positive number")
    private BigDecimal extraPrice;

    private List<MultipartFile> images;

    private List<Long> deletedImageIds;

    // If this is set, an existing image becomes main
    private Long mainImageId;

    // If the user picks a BRAND NEW upload as main,
    // we use the index of the 'images' list
    private Integer newMainImageIndex;
}
