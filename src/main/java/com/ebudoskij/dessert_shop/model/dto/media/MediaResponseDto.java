package com.ebudoskij.dessert_shop.model.dto.media;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MediaResponseDto {
    private Long id;

    private String url;

    private Integer priority;
}
