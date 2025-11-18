package com.organicnow.backend.unit.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.organicnow.backend.dto.PackagePlanRequestDto;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class PackagePlanRequestDtoTest {

    private static Validator validator;

    private ObjectMapper mapper() {
        ObjectMapper m = new ObjectMapper();
        m.registerModule(new JavaTimeModule());
        return m;
    }

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testGetterSetter() {
        PackagePlanRequestDto dto = new PackagePlanRequestDto();

        dto.setContractTypeId(10L);
        dto.setPrice(new BigDecimal("2000.00"));
        dto.setIsActive(1);
        dto.setRoomSize(35);

        assertEquals(10L, dto.getContractTypeId());
        assertEquals(new BigDecimal("2000.00"), dto.getPrice());
        assertEquals(1, dto.getIsActive());
        assertEquals(35, dto.getRoomSize());
    }

    @Test
    void testJsonDeserialization_withJsonAlias() throws Exception {
        String json = """
                {
                    "contract_type_id": 5,
                    "price": 1500,
                    "is_active": 1,
                    "room_size": 28
                }
                """;

        PackagePlanRequestDto dto = mapper().readValue(json, PackagePlanRequestDto.class);

        assertEquals(5L, dto.getContractTypeId());
        assertEquals(new BigDecimal("1500"), dto.getPrice());
        assertEquals(1, dto.getIsActive());
        assertEquals(28, dto.getRoomSize());
    }

    @Test
    void testJsonSerialization() throws Exception {
        PackagePlanRequestDto dto = new PackagePlanRequestDto(
                8L,
                new BigDecimal("3200"),
                1,
                45
        );

        String json = mapper().writeValueAsString(dto);
        assertTrue(json.contains("\"contractTypeId\":8"));
        assertTrue(json.contains("\"price\":3200"));
        assertTrue(json.contains("\"isActive\":1"));
        assertTrue(json.contains("\"roomSize\":45"));
    }

    @Test
    void testValidation_success() {
        PackagePlanRequestDto dto = new PackagePlanRequestDto(
                3L,
                new BigDecimal("900"),
                1,
                30
        );

        Set<ConstraintViolation<PackagePlanRequestDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testValidation_missingFields() {
        PackagePlanRequestDto dto = new PackagePlanRequestDto();

        Set<ConstraintViolation<PackagePlanRequestDto>> violations = validator.validate(dto);

        assertEquals(4, violations.size()); // ทั้ง 4 ฟิลด์เป็น @NotNull
    }

    @Test
    void testValidation_negativePrice_shouldFail() {
        PackagePlanRequestDto dto = new PackagePlanRequestDto(
                2L,
                new BigDecimal("-500"), // ❌ invalid
                1,
                20
        );

        Set<ConstraintViolation<PackagePlanRequestDto>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("price"))
        );
    }

    @Test
    void testJsonIgnoreUnknownProperties() throws Exception {
        String json = """
                {
                    "contractTypeId": 4,
                    "price": 2000,
                    "isActive": 1,
                    "roomSize": 32,
                    "extraField": "IGNORED",
                    "unexpected": 123
                }
                """;

        PackagePlanRequestDto dto = mapper().readValue(json, PackagePlanRequestDto.class);

        assertEquals(4L, dto.getContractTypeId());
        assertEquals(new BigDecimal("2000"), dto.getPrice());
        assertEquals(1, dto.getIsActive());
        assertEquals(32, dto.getRoomSize());
    }
}
