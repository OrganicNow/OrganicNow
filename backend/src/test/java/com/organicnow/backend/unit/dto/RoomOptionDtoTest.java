package com.organicnow.backend.unit.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.organicnow.backend.dto.RoomOptionDto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoomOptionDtoTest {

    @Test
    void testNoArgsConstructorAndSetters() {
        RoomOptionDto dto = new RoomOptionDto();

        dto.setId(1L);
        dto.setRoomNumber("A101");
        dto.setRoomFloor(3);
        dto.setStatus("Occupied");
        dto.setRoomSize("Studio");

        assertEquals(1L, dto.getId());
        assertEquals("A101", dto.getRoomNumber());
        assertEquals(3, dto.getRoomFloor());
        assertEquals("Occupied", dto.getStatus());
        assertEquals("Studio", dto.getRoomSize());
    }

    @Test
    void testAllArgsConstructor() {
        RoomOptionDto dto = new RoomOptionDto(
                2L,
                "B202",
                5,
                "Available",
                "1 Bedroom"
        );

        assertEquals(2L, dto.getId());
        assertEquals("B202", dto.getRoomNumber());
        assertEquals(5, dto.getRoomFloor());
        assertEquals("Available", dto.getStatus());
        assertEquals("1 Bedroom", dto.getRoomSize());
    }

    @Test
    void testEqualsAndHashCode() {
        RoomOptionDto dto1 = new RoomOptionDto(10L, "C303", 7, "Occupied", "Studio");
        RoomOptionDto dto2 = new RoomOptionDto(10L, "C303", 7, "Occupied", "Studio");

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());

        dto2.setStatus("Changed");
        assertNotEquals(dto1, dto2);
    }

    @Test
    void testToStringContainsFields() {
        RoomOptionDto dto = new RoomOptionDto(20L, "D404", 8, "Maintenance", "2 Bedroom");

        String str = dto.toString();

        assertTrue(str.contains("D404"));
        assertTrue(str.contains("Maintenance"));
        assertTrue(str.contains("2 Bedroom"));
    }

    @Test
    void testJsonSerializationAndDeserialization() throws Exception {
        RoomOptionDto original = new RoomOptionDto(
                30L,
                "E505",
                12,
                "Available",
                "Studio"
        );

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        String json = mapper.writeValueAsString(original);
        RoomOptionDto cloned = mapper.readValue(json, RoomOptionDto.class);

        assertEquals(original, cloned);
        assertEquals(original.getRoomNumber(), cloned.getRoomNumber());
    }

    @Test
    void testPartialData() {
        RoomOptionDto dto = new RoomOptionDto();
        dto.setId(99L);

        assertEquals(99L, dto.getId());
        assertNull(dto.getRoomNumber());
        assertNull(dto.getStatus());
    }
}
