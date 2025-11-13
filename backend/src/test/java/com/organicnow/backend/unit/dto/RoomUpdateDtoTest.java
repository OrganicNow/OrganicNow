//package com.organicnow.backend.dto;
//import org.junit.jupiter.api.Test;
//import static org.junit.jupiter.api.Assertions.*;
//
//public class RoomUpdateDtoTest {
//
//    @Test
//    void testRoomUpdateDtoConstructor() {
//        // Arrange
//        Integer roomFloor = 3;
//        String roomNumber = "C305";
//
//        // Act
//        RoomUpdateDto dto = new RoomUpdateDto();
//        dto.setRoomFloor(roomFloor);
//        dto.setRoomNumber(roomNumber);
//
//        // Assert
//        assertEquals(roomFloor, dto.getRoomFloor(), "Room floor should be set and retrieved correctly");
//        assertEquals(roomNumber, dto.getRoomNumber(), "Room number should be set and retrieved correctly");
//    }
//
//    @Test
//    void testRoomUpdateDtoNoArgsConstructor() {
//        // Act
//        RoomUpdateDto dto = new RoomUpdateDto();
//
//        // Assert
//        assertNull(dto.getRoomFloor(), "Room floor should be null by default");
//        assertNull(dto.getRoomNumber(), "Room number should be null by default");
//    }
//
//    @Test
//    void testSetterAndGetter() {
//        // Arrange
//        RoomUpdateDto dto = new RoomUpdateDto();
//        Integer roomFloor = 4;
//        String roomNumber = "D406";
//
//        // Act
//        dto.setRoomFloor(roomFloor);
//        dto.setRoomNumber(roomNumber);
//
//        // Assert
//        assertEquals(roomFloor, dto.getRoomFloor(), "Room floor should be updated correctly");
//        assertEquals(roomNumber, dto.getRoomNumber(), "Room number should be updated correctly");
//    }
//}
//
