//package com.organicnow.backend.dto;
//import org.junit.jupiter.api.Test;
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import static org.junit.jupiter.api.Assertions.*;
//
//public class UpdateTenantContractRequestTest {
//
//    @Test
//    void testUpdateTenantContractRequestConstructorAndGettersSetters() {
//        // Arrange: กำหนดค่าต่างๆ สำหรับทดสอบ
//        String firstName = "John";
//        String lastName = "Doe";
//        String email = "john.doe@example.com";
//        String phoneNumber = "1234567890";
//        String nationalId = "1234567890123";
//
//        Long roomId = 101L;
//        Long packageId = 5L;
//
//        LocalDateTime signDate = LocalDateTime.of(2025, 10, 14, 12, 0, 0, 0);
//        LocalDateTime startDate = LocalDateTime.of(2025, 10, 15, 9, 0, 0, 0);
//        LocalDateTime endDate = LocalDateTime.of(2026, 10, 14, 9, 0, 0, 0);
//
//        Integer status = 1;  // 1 = Active
//        BigDecimal deposit = new BigDecimal("500.00");
//        BigDecimal rentAmountSnapshot = new BigDecimal("1500.00");
//
//        // Act: สร้าง object โดยใช้ constructor
//        UpdateTenantContractRequest request = new UpdateTenantContractRequest(
//                firstName, lastName, email, phoneNumber, nationalId,
//                roomId, packageId, signDate, startDate, endDate,
//                status, deposit, rentAmountSnapshot
//        );
//
//        // Assert: ตรวจสอบว่า getter และ setter ทำงานถูกต้อง
//        assertEquals(firstName, request.getFirstName(), "First name should match");
//        assertEquals(lastName, request.getLastName(), "Last name should match");
//        assertEquals(email, request.getEmail(), "Email should match");
//        assertEquals(phoneNumber, request.getPhoneNumber(), "Phone number should match");
//        assertEquals(nationalId, request.getNationalId(), "National ID should match");
//
//        assertEquals(roomId, request.getRoomId(), "Room ID should match");
//        assertEquals(packageId, request.getPackageId(), "Package ID should match");
//
//        assertEquals(signDate, request.getSignDate(), "Sign date should match");
//        assertEquals(startDate, request.getStartDate(), "Start date should match");
//        assertEquals(endDate, request.getEndDate(), "End date should match");
//
//        assertEquals(status, request.getStatus(), "Status should match");
//        assertEquals(deposit, request.getDeposit(), "Deposit should match");
//        assertEquals(rentAmountSnapshot, request.getRentAmountSnapshot(), "Rent amount snapshot should match");
//
//        // Testing with nullable fields (testing null values)
//        UpdateTenantContractRequest nullRequest = new UpdateTenantContractRequest();
//        assertNull(nullRequest.getFirstName(), "First name should be null");
//        assertNull(nullRequest.getLastName(), "Last name should be null");
//        assertNull(nullRequest.getEmail(), "Email should be null");
//        assertNull(nullRequest.getPhoneNumber(), "Phone number should be null");
//        assertNull(nullRequest.getNationalId(), "National ID should be null");
//
//        assertNull(nullRequest.getRoomId(), "Room ID should be null");
//        assertNull(nullRequest.getPackageId(), "Package ID should be null");
//
//        assertNull(nullRequest.getSignDate(), "Sign date should be null");
//        assertNull(nullRequest.getStartDate(), "Start date should be null");
//        assertNull(nullRequest.getEndDate(), "End date should be null");
//
//        assertNull(nullRequest.getStatus(), "Status should be null");
//        assertNull(nullRequest.getDeposit(), "Deposit should be null");
//        assertNull(nullRequest.getRentAmountSnapshot(), "Rent amount snapshot should be null");
//
//        // Testing setters for nullable fields
//        nullRequest.setFirstName("Jane");
//        assertEquals("Jane", nullRequest.getFirstName(), "First name should be updated");
//
//        nullRequest.setRoomId(102L);
//        assertEquals(102L, nullRequest.getRoomId(), "Room ID should be updated");
//    }
//}
//
