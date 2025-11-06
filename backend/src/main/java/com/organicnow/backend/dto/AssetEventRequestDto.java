package com.organicnow.backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class AssetEventRequestDto {
    private List<Long> assetIds;   // asset ids ทั้งหมดของห้อง (หลังอัปเดต)
    private String reasonType;     // addon / damage / free
    private String note;           // หมายเหตุเพิ่มเติม
}