//package com.organicnow.backend.dto;
//import org.junit.jupiter.api.Test;
//import java.math.BigDecimal;
//import static org.junit.jupiter.api.Assertions.*;
//
//public class PackagePlanRequestDtoTest {
//
//    @Test
//    void testPackagePlanRequestDtoConstructor() {
//        // Arrange
//        BigDecimal price = new BigDecimal("199.99");
//        Integer isActive = 1;
//        Long contractTypeId = 2L;
//
//        // Act
//        PackagePlanRequestDto dto = new PackagePlanRequestDto();
//        dto.setPrice(price);
//        dto.setIsActive(isActive);
//        dto.setContractTypeId(contractTypeId);
//
//        // Assert
//        assertEquals(price, dto.getPrice());
//        assertEquals(isActive, dto.getIsActive());
//        assertEquals(contractTypeId, dto.getContractTypeId());
//    }
//
//    @Test
//    void testPackagePlanRequestDtoNoArgsConstructor() {
//        // Act
//        PackagePlanRequestDto dto = new PackagePlanRequestDto();
//
//        // Assert
//        assertNull(dto.getPrice(), "Price should be null by default");
//        assertNull(dto.getIsActive(), "IsActive should be null by default");
//        assertNull(dto.getContractTypeId(), "ContractTypeId should be null by default");
//    }
//
//    @Test
//    void testSetterAndGetter() {
//        // Arrange
//        PackagePlanRequestDto dto = new PackagePlanRequestDto();
//        BigDecimal price = new BigDecimal("299.99");
//        Integer isActive = 0;
//        Long contractTypeId = 3L;
//
//        // Act
//        dto.setPrice(price);
//        dto.setIsActive(isActive);
//        dto.setContractTypeId(contractTypeId);
//
//        // Assert
//        assertEquals(price, dto.getPrice(), "Price should be updated correctly");
//        assertEquals(isActive, dto.getIsActive(), "IsActive should be updated correctly");
//        assertEquals(contractTypeId, dto.getContractTypeId(), "ContractTypeId should be updated correctly");
//    }
//}
//
