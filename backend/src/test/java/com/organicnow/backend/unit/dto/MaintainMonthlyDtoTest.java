package com.organicnow.backend.unit.dto;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.organicnow.backend.dto.MaintainMonthlyDto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MaintainMonthlyDtoTest {

    private ObjectMapper mapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    @Test
    void testNoArgsConstructor() {
        MaintainMonthlyDto dto = new MaintainMonthlyDto();
        assertNotNull(dto);
        assertNull(dto.getMonth());
        assertNull(dto.getTotal());
    }

    @Test
    void testAllArgsConstructor() {
        MaintainMonthlyDto dto = new MaintainMonthlyDto("2025-01", 12L);

        assertEquals("2025-01", dto.getMonth());
        assertEquals(12L, dto.getTotal());
    }

    @Test
    void testSettersAndGetters() {
        MaintainMonthlyDto dto = new MaintainMonthlyDto();

        dto.setMonth("2025-02");
        dto.setTotal(5L);

        assertEquals("2025-02", dto.getMonth());
        assertEquals(5L, dto.getTotal());
    }

    @Test
    void testEqualsAndHashCode() {
        MaintainMonthlyDto dto1 = new MaintainMonthlyDto("2025-03", 7L);
        MaintainMonthlyDto dto2 = new MaintainMonthlyDto("2025-03", 7L);

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testNotEquals() {
        MaintainMonthlyDto dto1 = new MaintainMonthlyDto("2025-03", 7L);
        MaintainMonthlyDto dto2 = new MaintainMonthlyDto("2025-04", 7L);

        assertNotEquals(dto1, dto2);
    }

    @Test
    void testJsonSerializationDeserialization() throws Exception {
        MaintainMonthlyDto original = new MaintainMonthlyDto("2025-04", 9L);

        String json = mapper().writeValueAsString(original);
        MaintainMonthlyDto cloned = mapper().readValue(json, MaintainMonthlyDto.class);

        assertEquals(original, cloned);
        assertEquals(original.getMonth(), cloned.getMonth());
        assertEquals(original.getTotal(), cloned.getTotal());
    }

    @Test
    void testToString() {
        MaintainMonthlyDto dto = new MaintainMonthlyDto("2025-06", 20L);
        String str = dto.toString();
        assertTrue(str.contains("month=2025-06"));
        assertTrue(str.contains("total=20"));
    }
}
