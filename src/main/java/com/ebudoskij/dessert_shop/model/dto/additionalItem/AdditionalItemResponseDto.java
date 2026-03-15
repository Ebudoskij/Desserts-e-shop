package com.ebudoskij.dessert_shop.model.dto.additionalItem;

import com.ebudoskij.dessert_shop.model.dto.media.MediaResponseDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdditionalItemResponseDto {
    @NotNull
    private Long id;

    @NotBlank
    private String name;

    private String description;

    @Positive
    private BigDecimal extraPrice;

    private List<MediaResponseDto> imageUrls;
}
