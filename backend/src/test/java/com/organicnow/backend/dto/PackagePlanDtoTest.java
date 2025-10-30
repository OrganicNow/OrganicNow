//package com.organicnow.backend.dto;
//import org.junit.jupiter.api.Test;
//import java.math.BigDecimal;
//import static org.junit.jupiter.api.Assertions.*;
//
//public class PackagePlanDtoTest {
//
//    @Test
//    void testPackagePlanDtoConstructor() {
//        // Arrange
//        Long id = 1L;
//        BigDecimal price = new BigDecimal("99.99");
//        Integer isActive = 1;
//        String name = "Basic Plan";
//        Integer duration = 12;
//        Long contractTypeId = 2L;
//        String contractTypeName = "Yearly";
//
//        // Act
//        PackagePlanDto dto = new PackagePlanDto(id, price, isActive, name, duration);
//        dto.setContractTypeId(contractTypeId);
//        dto.setContractTypeName(contractTypeName);
//
//        // Assert
//        assertEquals(id, dto.getId());
//        assertEquals(price, dto.getPrice());
//        assertEquals(isActive, dto.getIsActive());
//        assertEquals(name, dto.getName());
//        assertEquals(duration, dto.getDuration());
//        assertEquals(contractTypeId, dto.getContractTypeId());
//        assertEquals(contractTypeName, dto.getContractTypeName());
//    }
//
//    @Test
//    void testPackagePlanDtoNoArgsConstructor() {
//        // Act
//        PackagePlanDto dto = new PackagePlanDto();
//
//        // Assert
//        assertNull(dto.getId());
//        assertNull(dto.getPrice());
//        assertNull(dto.getIsActive());
//        assertNull(dto.getName());
//        assertNull(dto.getDuration());
//        assertNull(dto.getContractTypeId());
//        assertNull(dto.getContractTypeName());
//    }
//
//    @Test
//    void testSetterAndGetter() {
//        // Arrange
//        PackagePlanDto dto = new PackagePlanDto();
//        Long id = 3L;
//        BigDecimal price = new BigDecimal("199.99");
//        Integer isActive = 0;
//        String name = "Premium Plan";
//        Integer duration = 24;
//        Long contractTypeId = 1L;
//        String contractTypeName = "Monthly";
//
//        // Act
//        dto.setId(id);
//        dto.setPrice(price);
//        dto.setIsActive(isActive);
//        dto.setName(name);
//        dto.setDuration(duration);
//        dto.setContractTypeId(contractTypeId);
//        dto.setContractTypeName(contractTypeName);
//
//        // Assert
//        assertEquals(id, dto.getId());
//        assertEquals(price, dto.getPrice());
//        assertEquals(isActive, dto.getIsActive());
//        assertEquals(name, dto.getName());
//        assertEquals(duration, dto.getDuration());
//        assertEquals(contractTypeId, dto.getContractTypeId());
//        assertEquals(contractTypeName, dto.getContractTypeName());
//    }
//}
