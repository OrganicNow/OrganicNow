package com.organicnow.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomOptionDto {
    private Long id;
    private String roomNumber;
    private Integer roomFloor;
    private String status;
    private String roomSize;// occupied / available
}
