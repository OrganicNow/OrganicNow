//package com.organicnow.backend.dto;
//import org.junit.jupiter.api.Test;
//import static org.junit.jupiter.api.Assertions.*;
//
//public class RoomOptionDtoTest {
//
//    @Test
//    void testRoomOptionDtoConstructor() {
//        // Arrange
//        Long id = 1L;
//        String roomNumber = "A101";
//        Integer roomFloor = 1;
//        String status = "available";
//
//        // Act
//        RoomOptionDto dto = new RoomOptionDto(id, roomNumber, roomFloor, status);
//
//        // Assert
//        assertEquals(id, dto.getId());
//        assertEquals(roomNumber, dto.getRoomNumber());
//        assertEquals(roomFloor, dto.getRoomFloor());
//        assertEquals(status, dto.getStatus());
//    }
//
//    @Test
//    void testRoomOptionDtoNoArgsConstructor() {
//        // Act
//        RoomOptionDto dto = new RoomOptionDto();
//
//        // Assert
//        assertNull(dto.getId(), "ID should be null by default");
//        assertNull(dto.getRoomNumber(), "Room number should be null by default");
//        assertNull(dto.getRoomFloor(), "Room floor should be null by default");
//        assertNull(dto.getStatus(), "Status should be null by default");
//    }
//
//    @Test
//    void testSetterAndGetter() {
//        // Arrange
//        RoomOptionDto dto = new RoomOptionDto();
//        Long id = 2L;
//        String roomNumber = "B202";
//        Integer roomFloor = 2;
//        String status = "occupied";
//
//        // Act
//        dto.setId(id);
//        dto.setRoomNumber(roomNumber);
//        dto.setRoomFloor(roomFloor);
//        dto.setStatus(status);
//
//        // Assert
//        assertEquals(id, dto.getId(), "ID should be set and retrieved correctly");
//        assertEquals(roomNumber, dto.getRoomNumber(), "Room number should be set and retrieved correctly");
//        assertEquals(roomFloor, dto.getRoomFloor(), "Room floor should be set and retrieved correctly");
//        assertEquals(status, dto.getStatus(), "Status should be set and retrieved correctly");
//    }
//}
