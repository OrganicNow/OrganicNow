//package com.organicnow.backend.dto;
//import org.junit.jupiter.api.Test;
//import static org.junit.jupiter.api.Assertions.*;
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//
//public class TenantDtoTest {
//
//    @Test
//    void testTenantDtoConstructor() {
//        // Arrange
//        Long contractId = 1L;
//        String firstName = "John";
//        String lastName = "Doe";
//        String phoneNumber = "1234567890";
//        String email = "john.doe@example.com";
//        String nationalId = "123456789"; // เพิ่มค่าของ nationalId
//        String room = "A305";
//        Integer floor = 3;
//        Long roomId = 101L;
//        Long packageId = 5L;
//        Long contractTypeId = 2L;
//        String contractName = "Monthly Lease";
//
//        // กำหนดค่า signDate, startDate และ endDate ที่คาดหวัง
//        LocalDateTime signDate = LocalDateTime.of(2025, 10, 14, 12, 0, 0, 0);
//        LocalDateTime startDate = LocalDateTime.of(2025, 10, 15, 9, 0, 0, 0);
//        LocalDateTime endDate = LocalDateTime.of(2026, 10, 14, 9, 0, 0, 0);
//
//        BigDecimal deposit = new BigDecimal("500.00");
//        BigDecimal rentAmountSnapshot = new BigDecimal("1500.00");
//        Integer status = 1; // Active
//
//        // Act: สร้างอ็อบเจ็กต์จาก constructor ที่ใช้พารามิเตอร์ครบถ้วน
//        TenantDto dto = new TenantDto(
//                contractId, firstName, lastName, floor, room, roomId, packageId, contractTypeId,
//                contractName, startDate, endDate, phoneNumber, email, nationalId, status
//        );
//
//        // กำหนดค่า signDate โดยตรงใน Unit Test
//        dto.setSignDate(signDate);  // เพิ่มการตั้งค่า signDate ที่จำเป็นใน Unit Test
//
//        // Assert
//        assertNotNull(dto.getSignDate(), "Sign date should not be null");
//        assertEquals(signDate, dto.getSignDate(), "Sign date should match");
//        assertEquals(startDate, dto.getStartDate(), "Start date should match");
//        assertEquals(endDate, dto.getEndDate(), "End date should match");
//    }
//
//
//
//    @Test
//    void testSetterAndGetter() {
//        // Arrange
//        TenantDto dto = new TenantDto();
//        Long contractId = 2L;
//        String firstName = "Jane";
//        String lastName = "Smith";
//        String phoneNumber = "0987654321";
//        String email = "jane.smith@example.com";
//        String room = "B202";
//        Integer floor = 2;
//        Long roomId = 102L;
//        Long packageId = 6L;
//        Long contractTypeId = 3L;
//        String contractName = "Yearly Lease";
//        LocalDateTime startDate = LocalDateTime.of(2025, 11, 5, 9, 0, 0, 0);
//        LocalDateTime endDate = LocalDateTime.of(2026, 11, 4, 9, 0, 0, 0);
//        LocalDateTime signDate = LocalDateTime.of(2025, 11, 1, 12, 0, 0, 0);
//        BigDecimal deposit = new BigDecimal("300.00");
//        BigDecimal rentAmountSnapshot = new BigDecimal("1200.00");
//        Integer status = 0; // Inactive
//
//        // Act
//        dto.setContractId(contractId);
//        dto.setFirstName(firstName);
//        dto.setLastName(lastName);
//        dto.setPhoneNumber(phoneNumber);
//        dto.setEmail(email);
//        dto.setRoom(room);
//        dto.setFloor(floor);
//        dto.setRoomId(roomId);
//        dto.setPackageId(packageId);
//        dto.setContractTypeId(contractTypeId);
//        dto.setContractName(contractName);
//        dto.setStartDate(startDate);
//        dto.setEndDate(endDate);
//        dto.setSignDate(signDate);
//        dto.setDeposit(deposit);
//        dto.setRentAmountSnapshot(rentAmountSnapshot);
//        dto.setStatus(status);
//
//        // Assert
//        assertEquals(contractId, dto.getContractId());
//        assertEquals(firstName, dto.getFirstName());
//        assertEquals(lastName, dto.getLastName());
//        assertEquals(phoneNumber, dto.getPhoneNumber());
//        assertEquals(email, dto.getEmail());
//        assertEquals(room, dto.getRoom());
//        assertEquals(floor, dto.getFloor());
//        assertEquals(roomId, dto.getRoomId());
//        assertEquals(packageId, dto.getPackageId());
//        assertEquals(contractTypeId, dto.getContractTypeId());
//        assertEquals(contractName, dto.getContractName());
//        assertEquals(startDate, dto.getStartDate());
//        assertEquals(endDate, dto.getEndDate());
//        assertEquals(signDate, dto.getSignDate());
//        assertEquals(deposit, dto.getDeposit());
//        assertEquals(rentAmountSnapshot, dto.getRentAmountSnapshot());
//        assertEquals(status, dto.getStatus());
//    }
//}
//
