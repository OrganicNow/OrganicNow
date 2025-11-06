package com.organicnow.backend.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetEventDto {
    private Long eventId;
    private Long roomId;
    private Long assetId;
    private String assetName;
    private String eventType;
    private String reasonType;
    private String note;
    private LocalDateTime createdAt;
}