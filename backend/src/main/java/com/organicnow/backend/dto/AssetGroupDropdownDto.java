package com.organicnow.backend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetGroupDropdownDto {
    private Long id;
    private String name;
    private Integer threshold = 5;
}
