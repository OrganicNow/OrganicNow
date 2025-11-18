package com.organicnow.backend.unit.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.organicnow.backend.dto.PackagePlanDto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class PackagePlanDtoTest {

    private ObjectMapper mapper() {
        ObjectMapper m = new ObjectMapper();
        m.registerModule(new JavaTimeModule());
        return m;
    }

    @Test
    void testAllArgsConstructor() {
        PackagePlanDto dto = new PackagePlanDto(
                1L,
                new BigDecimal("999.99"),
                1,
                "Standard Package",
                12,
                2L,
                "Long Term",
                35
        );

        assertEquals(1L, dto.getId());
        assertEquals(new BigDecimal("999.99"), dto.getPrice());
        assertEquals(1, dto.getIsActive());
        assertEquals("Standard Package", dto.getName());
        assertEquals(12, dto.getDuration());
        assertEquals(2L, dto.getContractTypeId());
        assertEquals("Long Term", dto.getContractTypeName());
        assertEquals(35, dto.getRoomSize());
    }

    @Test
    void testGetterSetter() {
        PackagePlanDto dto = new PackagePlanDto();

        dto.setId(5L);
        dto.setPrice(new BigDecimal("1500.00"));
        dto.setIsActive(0);
        dto.setName("Premium Package");
        dto.setDuration(6);
        dto.setContractTypeId(10L);
        dto.setContractTypeName("Short Term");
        dto.setRoomSize(25);

        assertEquals(5L, dto.getId());
        assertEquals(new BigDecimal("1500.00"), dto.getPrice());
        assertEquals(0, dto.getIsActive());
        assertEquals("Premium Package", dto.getName());
        assertEquals(6, dto.getDuration());
        assertEquals(10L, dto.getContractTypeId());
        assertEquals("Short Term", dto.getContractTypeName());
        assertEquals(25, dto.getRoomSize());
    }

    @Test
    void testJsonSerializationDeserialization() throws Exception {
        PackagePlanDto original = new PackagePlanDto(
                2L,
                new BigDecimal("4500"),
                1,
                "VIP Package",
                24,
                3L,
                "Exclusive",
                40
        );

        String json = mapper().writeValueAsString(original);
        PackagePlanDto clone = mapper().readValue(json, PackagePlanDto.class);

        assertEquals(original.getId(), clone.getId());
        assertEquals(original.getPrice(), clone.getPrice());
        assertEquals(original.getIsActive(), clone.getIsActive());
        assertEquals(original.getName(), clone.getName());
        assertEquals(original.getDuration(), clone.getDuration());
        assertEquals(original.getContractTypeId(), clone.getContractTypeId());
        assertEquals(original.getContractTypeName(), clone.getContractTypeName());
        assertEquals(original.getRoomSize(), clone.getRoomSize());
    }

    @Test
    void testJsonPropertyMapping() throws Exception {
        String json = """
                {
                    "id": 9,
                    "price": 2500,
                    "is_active": 1,
                    "contract_name": "Luxury",
                    "duration": 18,
                    "contract_type_id": 8,
                    "contract_type_name": "Corporate",
                    "room_size": 55
                }
                """;

        PackagePlanDto dto = mapper().readValue(json, PackagePlanDto.class);

        assertEquals(9L, dto.getId());
        assertEquals(new BigDecimal("2500"), dto.getPrice());
        assertEquals(1, dto.getIsActive());
        assertEquals("Luxury", dto.getName());
        assertEquals(18, dto.getDuration());
        assertEquals(8L, dto.getContractTypeId());
        assertEquals("Corporate", dto.getContractTypeName());
        assertEquals(55, dto.getRoomSize());
    }

    @Test
    void testNullSafety() {
        PackagePlanDto dto = new PackagePlanDto();

        assertNull(dto.getId());
        assertNull(dto.getPrice());
        assertNull(dto.getIsActive());
        assertNull(dto.getName());
        assertNull(dto.getDuration());
        assertNull(dto.getContractTypeId());
        assertNull(dto.getContractTypeName());
        assertNull(dto.getRoomSize());
    }
}
