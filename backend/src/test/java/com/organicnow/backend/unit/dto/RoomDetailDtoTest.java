//package com.organicnow.backend.dto;
//import org.junit.jupiter.api.Test;
//import java.time.LocalDateTime;
//import java.util.Arrays;
//import static org.junit.jupiter.api.Assertions.*;
//
//public class RoomDetailDtoTest {
//
//    @Test
//    void testRoomDetailDtoConstructor() {
//        // Arrange
//        Long roomId = 101L;
//        String roomNumber = "A101";
//        int roomFloor = 1;
//        String status = "Occupied";
//        String firstName = "John";
//        String lastName = "Doe";
//        String phoneNumber = "1234567890";
//        String email = "john.doe@example.com";
//        String contractTypeName = "Monthly";
//        LocalDateTime signDate = LocalDateTime.of(2025, 10, 14, 12, 0, 0, 0);
//        LocalDateTime startDate = LocalDateTime.of(2025, 10, 15, 9, 0, 0, 0);
//        LocalDateTime endDate = LocalDateTime.of(2026, 10, 14, 9, 0, 0, 0);
//
//        // Act
//        RoomDetailDto dto = new RoomDetailDto(roomId, roomNumber, roomFloor, status,
//                firstName, lastName, phoneNumber, email, contractTypeName, signDate, startDate, endDate);
//
//        // Assert
//        assertEquals(roomId, dto.getRoomId());
//        assertEquals(roomNumber, dto.getRoomNumber());
//        assertEquals(roomFloor, dto.getRoomFloor());
//        assertEquals(status, dto.getStatus());
//        assertEquals(firstName, dto.getFirstName());
//        assertEquals(lastName, dto.getLastName());
//        assertEquals(phoneNumber, dto.getPhoneNumber());
//        assertEquals(email, dto.getEmail());
//        assertEquals(contractTypeName, dto.getContractTypeName());
//        assertEquals(signDate, dto.getSignDate());
//        assertEquals(startDate, dto.getStartDate());
//        assertEquals(endDate, dto.getEndDate());
//    }
//
//    @Test
//    void testRoomDetailDtoQueryConstructor() {
//        // Arrange
//        Long roomId = 102L;
//        String roomNumber = "B201";
//        int roomFloor = 2;
//        String status = "Vacant";
//        String firstName = "Jane";
//        String lastName = "Smith";
//        String phoneNumber = "0987654321";
//        String email = "jane.smith@example.com";
//        String contractTypeName = "Yearly";
//        LocalDateTime signDate = LocalDateTime.of(2025, 11, 1, 12, 0, 0, 0);
//        LocalDateTime startDate = LocalDateTime.of(2025, 11, 15, 9, 0, 0, 0);
//        LocalDateTime endDate = LocalDateTime.of(2026, 11, 14, 9, 0, 0, 0);
//
//        // Act
//        RoomDetailDto dto = new RoomDetailDto(roomId, roomNumber, roomFloor, status,
//                firstName, lastName, phoneNumber, email, contractTypeName, signDate, startDate, endDate);
//
//        // Assert
//        assertEquals(roomId, dto.getRoomId());
//        assertEquals(roomNumber, dto.getRoomNumber());
//        assertEquals(roomFloor, dto.getRoomFloor());
//        assertEquals(status, dto.getStatus());
//        assertEquals(firstName, dto.getFirstName());
//        assertEquals(lastName, dto.getLastName());
//        assertEquals(phoneNumber, dto.getPhoneNumber());
//        assertEquals(email, dto.getEmail());
//        assertEquals(contractTypeName, dto.getContractTypeName());
//        assertEquals(signDate, dto.getSignDate());
//        assertEquals(startDate, dto.getStartDate());
//        assertEquals(endDate, dto.getEndDate());
//    }
//
//    @Test
//    void testSetterAndGetter() {
//        // Arrange
//        RoomDetailDto dto = new RoomDetailDto();
//        Long roomId = 103L;
//        String roomNumber = "C301";
//        int roomFloor = 3;
//        String status = "Occupied";
//        String firstName = "Alice";
//        String lastName = "Brown";
//        String phoneNumber = "5551234567";
//        String email = "alice.brown@example.com";
//        String contractTypeName = "Weekly";
//        LocalDateTime signDate = LocalDateTime.of(2025, 12, 1, 12, 0, 0, 0);
//        LocalDateTime startDate = LocalDateTime.of(2025, 12, 5, 9, 0, 0, 0);
//        LocalDateTime endDate = LocalDateTime.of(2026, 12, 4, 9, 0, 0, 0);
//
//        // Act
//        dto.setRoomId(roomId);
//        dto.setRoomNumber(roomNumber);
//        dto.setRoomFloor(roomFloor);
//        dto.setStatus(status);
//        dto.setFirstName(firstName);
//        dto.setLastName(lastName);
//        dto.setPhoneNumber(phoneNumber);
//        dto.setEmail(email);
//        dto.setContractTypeName(contractTypeName);
//        dto.setSignDate(signDate);
//        dto.setStartDate(startDate);
//        dto.setEndDate(endDate);
//
//        // Assert
//        assertEquals(roomId, dto.getRoomId());
//        assertEquals(roomNumber, dto.getRoomNumber());
//        assertEquals(roomFloor, dto.getRoomFloor());
//        assertEquals(status, dto.getStatus());
//        assertEquals(firstName, dto.getFirstName());
//        assertEquals(lastName, dto.getLastName());
//        assertEquals(phoneNumber, dto.getPhoneNumber());
//        assertEquals(email, dto.getEmail());
//        assertEquals(contractTypeName, dto.getContractTypeName());
//        assertEquals(signDate, dto.getSignDate());
//        assertEquals(startDate, dto.getStartDate());
//        assertEquals(endDate, dto.getEndDate());
//    }
//}
//
