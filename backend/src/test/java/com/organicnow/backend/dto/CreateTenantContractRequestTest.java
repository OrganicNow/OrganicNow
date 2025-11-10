package com.organicnow.backend.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CreateTenantContractRequestTest {

    @Test
    void testNoArgsConstructor() {
        // Act
        CreateTenantContractRequest request = new CreateTenantContractRequest();

        // Assert
        assertNull(request.getFirstName());
        assertNull(request.getLastName());
        assertNull(request.getEmail());
        assertNull(request.getPhoneNumber());
        assertNull(request.getNationalId());
        assertNull(request.getRoomId());
        assertNull(request.getPackageId());
        assertNull(request.getSignDate());
        assertNull(request.getStartDate());
        assertNull(request.getEndDate());
        assertNull(request.getDeposit());
        assertNull(request.getRentAmountSnapshot());
    }

    @Test
    void testAllArgsConstructor() {
        // Arrange
        String expectedFirstName = "John";
        String expectedLastName = "Doe";
        String expectedEmail = "john.doe@example.com";
        String expectedPhoneNumber = "1234567890";
        String expectedNationalId = "1234567890123";
        Long expectedRoomId = 1L;
        Long expectedPackageId = 2L;
        LocalDateTime expectedSignDate = LocalDateTime.of(2025, 1, 1, 10, 0, 0, 0);
        LocalDateTime expectedStartDate = LocalDateTime.of(2025, 2, 1, 0, 0, 0, 0);
        LocalDateTime expectedEndDate = LocalDateTime.of(2025, 12, 31, 0, 0, 0, 0);
        BigDecimal expectedDeposit = new BigDecimal("5000.00");
        BigDecimal expectedRentAmountSnapshot = new BigDecimal("10000.00");

        // Act
        CreateTenantContractRequest request = new CreateTenantContractRequest(
                expectedFirstName, expectedLastName, expectedEmail, expectedPhoneNumber, expectedNationalId,
                expectedRoomId, expectedPackageId, expectedSignDate, expectedStartDate, expectedEndDate,
                expectedDeposit, expectedRentAmountSnapshot
        );

        // Assert
        assertEquals(expectedFirstName, request.getFirstName());
        assertEquals(expectedLastName, request.getLastName());
        assertEquals(expectedEmail, request.getEmail());
        assertEquals(expectedPhoneNumber, request.getPhoneNumber());
        assertEquals(expectedNationalId, request.getNationalId());
        assertEquals(expectedRoomId, request.getRoomId());
        assertEquals(expectedPackageId, request.getPackageId());
        assertEquals(expectedSignDate, request.getSignDate());
        assertEquals(expectedStartDate, request.getStartDate());
        assertEquals(expectedEndDate, request.getEndDate());
        assertEquals(expectedDeposit, request.getDeposit());
        assertEquals(expectedRentAmountSnapshot, request.getRentAmountSnapshot());
    }

    @Test
    void testBuilder() {
        // Arrange
        String expectedFirstName = "Alice";
        String expectedLastName = "Smith";
        String expectedEmail = "alice.smith@example.com";
        String expectedPhoneNumber = "0987654321";
        String expectedNationalId = "9876543210987";
        Long expectedRoomId = 3L;
        Long expectedPackageId = 4L;
        LocalDateTime expectedSignDate = LocalDateTime.of(2025, 3, 15, 12, 0, 0, 0);
        LocalDateTime expectedStartDate = LocalDateTime.of(2025, 4, 1, 0, 0, 0, 0);
        LocalDateTime expectedEndDate = LocalDateTime.of(2026, 3, 31, 0, 0, 0, 0);
        BigDecimal expectedDeposit = new BigDecimal("3000.00");
        BigDecimal expectedRentAmountSnapshot = new BigDecimal("7000.00");

        // Act
        CreateTenantContractRequest request = CreateTenantContractRequest.builder()
                .firstName(expectedFirstName)
                .lastName(expectedLastName)
                .email(expectedEmail)
                .phoneNumber(expectedPhoneNumber)
                .nationalId(expectedNationalId)
                .roomId(expectedRoomId)
                .packageId(expectedPackageId)
                .signDate(expectedSignDate)
                .startDate(expectedStartDate)
                .endDate(expectedEndDate)
                .deposit(expectedDeposit)
                .rentAmountSnapshot(expectedRentAmountSnapshot)
                .build();

        // Assert
        assertEquals(expectedFirstName, request.getFirstName());
        assertEquals(expectedLastName, request.getLastName());
        assertEquals(expectedEmail, request.getEmail());
        assertEquals(expectedPhoneNumber, request.getPhoneNumber());
        assertEquals(expectedNationalId, request.getNationalId());
        assertEquals(expectedRoomId, request.getRoomId());
        assertEquals(expectedPackageId, request.getPackageId());
        assertEquals(expectedSignDate, request.getSignDate());
        assertEquals(expectedStartDate, request.getStartDate());
        assertEquals(expectedEndDate, request.getEndDate());
        assertEquals(expectedDeposit, request.getDeposit());
        assertEquals(expectedRentAmountSnapshot, request.getRentAmountSnapshot());
    }

    @Test
    void testSettersAndGetters() {
        // Arrange
        CreateTenantContractRequest request = new CreateTenantContractRequest();
        String expectedFirstName = "Bob";
        String expectedLastName = "Johnson";

        // Act
        request.setFirstName(expectedFirstName);
        request.setLastName(expectedLastName);

        // Assert
        assertEquals(expectedFirstName, request.getFirstName());
        assertEquals(expectedLastName, request.getLastName());
    }
}
