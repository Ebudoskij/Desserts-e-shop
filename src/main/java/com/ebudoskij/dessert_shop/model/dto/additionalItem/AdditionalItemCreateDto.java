package com.ebudoskij.dessert_shop.model.dto.additionalItem;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class AdditionalItemCreateDto {
    @NotBlank
    private String name;

    private String description;

    @NotNull
    @Positive
    private BigDecimal extraPrice;

    private List<MultipartFile> images;
}
