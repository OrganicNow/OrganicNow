//package com.organicnow.backend.dto;
//import org.junit.jupiter.api.Test;
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.Arrays;
//import static org.junit.jupiter.api.Assertions.*;
//
//public class TenantDetailDtoTest {
//
//    @Test
//    void testTenantDetailDtoConstructor() {
//        // Arrange
//        Long contractId = 1L;
//        String firstName = "John";
//        String lastName = "Doe";
//        String email = "john.doe@example.com";
//        String phoneNumber = "1234567890";
//        String nationalId = "1234567890123";
//        Integer floor = 3;
//        String room = "A305";
//        String packageName = "Premium Package";
//        BigDecimal packagePrice = new BigDecimal("199.99");
//        LocalDateTime signDate = LocalDateTime.of(2025, 10, 14, 12, 0, 0, 0);
//        LocalDateTime startDate = LocalDateTime.of(2025, 10, 15, 9, 0, 0, 0);
//        LocalDateTime endDate = LocalDateTime.of(2026, 10, 14, 9, 0, 0, 0);
//        Integer status = 1;
//        BigDecimal deposit = new BigDecimal("500.00");
//        BigDecimal rentAmountSnapshot = new BigDecimal("1500.00");
//
//        // Invoice details
//        Long invoiceId = 101L;
//        LocalDateTime invoiceCreateDate = LocalDateTime.of(2025, 10, 15, 10, 0, 0, 0);
//        LocalDateTime invoiceDueDate = LocalDateTime.of(2025, 10, 20, 9, 0, 0, 0);
//        Integer invoiceStatus = 0;  // Pending
//        Integer netAmount = 1500;
//        LocalDateTime payDate = LocalDateTime.of(2025, 10, 18, 14, 0, 0, 0);
//        Integer payMethod = 1;  // Cash
//        Integer penaltyTotal = 0;
//        Integer subTotal = 1500;
//
//        // Create InvoiceDto
//        TenantDetailDto.InvoiceDto invoiceDto = new TenantDetailDto.InvoiceDto(
//                invoiceId, invoiceCreateDate, invoiceDueDate, invoiceStatus, netAmount, payDate, payMethod, penaltyTotal, subTotal
//        );
//        // Create list of invoices
//        var invoices = Arrays.asList(invoiceDto);
//
//        // Act
//        TenantDetailDto dto = new TenantDetailDto(
//                contractId, firstName, lastName, email, phoneNumber, nationalId, floor, room,
//                packageName, packagePrice, signDate, startDate, endDate, status, deposit, rentAmountSnapshot, invoices
//        );
//
//        // Assert
//        assertEquals(contractId, dto.getContractId());
//        assertEquals(firstName, dto.getFirstName());
//        assertEquals(lastName, dto.getLastName());
//        assertEquals(email, dto.getEmail());
//        assertEquals(phoneNumber, dto.getPhoneNumber());
//        assertEquals(nationalId, dto.getNationalId());
//        assertEquals(floor, dto.getFloor());
//        assertEquals(room, dto.getRoom());
//        assertEquals(packageName, dto.getPackageName());
//        assertEquals(packagePrice, dto.getPackagePrice());
//        assertEquals(signDate, dto.getSignDate());
//        assertEquals(startDate, dto.getStartDate());
//        assertEquals(endDate, dto.getEndDate());
//        assertEquals(status, dto.getStatus());
//        assertEquals(deposit, dto.getDeposit());
//        assertEquals(rentAmountSnapshot, dto.getRentAmountSnapshot());
//        assertEquals(1, dto.getInvoices().size(), "There should be one invoice");
//        assertEquals(invoiceDto, dto.getInvoices().get(0), "Invoice details should match");
//    }
//
//    @Test
//    void testSetterAndGetter() {
//        // Arrange
//        TenantDetailDto dto = new TenantDetailDto();
//        Long contractId = 2L;
//        String firstName = "Jane";
//        String lastName = "Smith";
//        String email = "jane.smith@example.com";
//        String phoneNumber = "0987654321";
//        String nationalId = "0987654321098";
//        Integer floor = 2;
//        String room = "B202";
//        String packageName = "Standard Package";
//        BigDecimal packagePrice = new BigDecimal("99.99");
//        LocalDateTime signDate = LocalDateTime.of(2025, 11, 1, 12, 0, 0, 0);
//        LocalDateTime startDate = LocalDateTime.of(2025, 11, 5, 9, 0, 0, 0);
//        LocalDateTime endDate = LocalDateTime.of(2026, 11, 4, 9, 0, 0, 0);
//        Integer status = 0;  // Inactive
//        BigDecimal deposit = new BigDecimal("300.00");
//        BigDecimal rentAmountSnapshot = new BigDecimal("1200.00");
//
//        // Act
//        dto.setContractId(contractId);
//        dto.setFirstName(firstName);
//        dto.setLastName(lastName);
//        dto.setEmail(email);
//        dto.setPhoneNumber(phoneNumber);
//        dto.setNationalId(nationalId);
//        dto.setFloor(floor);
//        dto.setRoom(room);
//        dto.setPackageName(packageName);
//        dto.setPackagePrice(packagePrice);
//        dto.setSignDate(signDate);
//        dto.setStartDate(startDate);
//        dto.setEndDate(endDate);
//        dto.setStatus(status);
//        dto.setDeposit(deposit);
//        dto.setRentAmountSnapshot(rentAmountSnapshot);
//
//        // Assert
//        assertEquals(contractId, dto.getContractId());
//        assertEquals(firstName, dto.getFirstName());
//        assertEquals(lastName, dto.getLastName());
//        assertEquals(email, dto.getEmail());
//        assertEquals(phoneNumber, dto.getPhoneNumber());
//        assertEquals(nationalId, dto.getNationalId());
//        assertEquals(floor, dto.getFloor());
//        assertEquals(room, dto.getRoom());
//        assertEquals(packageName, dto.getPackageName());
//        assertEquals(packagePrice, dto.getPackagePrice());
//        assertEquals(signDate, dto.getSignDate());
//        assertEquals(startDate, dto.getStartDate());
//        assertEquals(endDate, dto.getEndDate());
//        assertEquals(status, dto.getStatus());
//        assertEquals(deposit, dto.getDeposit());
//        assertEquals(rentAmountSnapshot, dto.getRentAmountSnapshot());
//    }
//}
//
