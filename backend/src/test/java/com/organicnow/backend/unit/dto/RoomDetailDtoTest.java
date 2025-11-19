package com.organicnow.backend.unit.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.organicnow.backend.dto.AssetDto;
import com.organicnow.backend.dto.RequestDto;
import com.organicnow.backend.dto.RoomDetailDto;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RoomDetailDtoTest {

    @Test
    void testNoArgsConstructorAndSetters() {
        RoomDetailDto dto = new RoomDetailDto();

        LocalDateTime signDate = LocalDateTime.of(2025, 1, 1, 10, 0);
        LocalDateTime startDate = LocalDateTime.of(2025, 2, 1, 12, 0);
        LocalDateTime endDate = LocalDateTime.of(2025, 12, 31, 18, 0);

        AssetDto asset1 = new AssetDto();
        asset1.setAssetId(1L);
        asset1.setAssetName("Air Conditioner");

        RequestDto req1 = new RequestDto();
        req1.setId(10L);
        req1.setIssueTitle("Water leak");

        dto.setRoomId(100L);
        dto.setRoomNumber("A101");
        dto.setRoomSize("Studio");
        dto.setRoomFloor(3);
        dto.setStatus("Occupied");
        dto.setFirstName("Alice");
        dto.setLastName("Smith");
        dto.setPhoneNumber("080-000-0000");
        dto.setEmail("alice@example.com");
        dto.setContractTypeName("Monthly");
        dto.setSignDate(signDate);
        dto.setStartDate(startDate);
        dto.setEndDate(endDate);
        dto.setAssets(List.of(asset1));
        dto.setRequests(List.of(req1));

        assertEquals(100L, dto.getRoomId());
        assertEquals("A101", dto.getRoomNumber());
        assertEquals("Studio", dto.getRoomSize());
        assertEquals(3, dto.getRoomFloor());
        assertEquals("Occupied", dto.getStatus());
        assertEquals("Alice", dto.getFirstName());
        assertEquals("Smith", dto.getLastName());
        assertEquals("080-000-0000", dto.getPhoneNumber());
        assertEquals("alice@example.com", dto.getEmail());
        assertEquals("Monthly", dto.getContractTypeName());
        assertEquals(signDate, dto.getSignDate());
        assertEquals(startDate, dto.getStartDate());
        assertEquals(endDate, dto.getEndDate());

        assertNotNull(dto.getAssets());
        assertEquals(1, dto.getAssets().size());
        assertEquals("Air Conditioner", dto.getAssets().get(0).getAssetName());

        assertNotNull(dto.getRequests());
        assertEquals(1, dto.getRequests().size());
        assertEquals("Water leak", dto.getRequests().get(0).getIssueTitle());
    }

    @Test
    void testAllArgsConstructor() {
        LocalDateTime signDate = LocalDateTime.of(2025, 1, 1, 10, 0);
        LocalDateTime startDate = LocalDateTime.of(2025, 2, 1, 12, 0);
        LocalDateTime endDate = LocalDateTime.of(2025, 12, 31, 18, 0);

        AssetDto asset1 = new AssetDto();
        asset1.setAssetId(1L);
        asset1.setAssetName("Table");

        RequestDto req1 = new RequestDto();
        req1.setId(20L);
        req1.setIssueTitle("Broken lamp");

        RoomDetailDto dto = new RoomDetailDto(
                200L,
                "B202",
                "1 Bedroom",
                5,
                "Vacant",
                "Bob",
                "Johnson",
                "081-111-1111",
                "bob@example.com",
                "Yearly",
                signDate,
                startDate,
                endDate,
                List.of(asset1),
                List.of(req1)
        );

        assertEquals(200L, dto.getRoomId());
        assertEquals("B202", dto.getRoomNumber());
        assertEquals("1 Bedroom", dto.getRoomSize());
        assertEquals(5, dto.getRoomFloor());
        assertEquals("Vacant", dto.getStatus());
        assertEquals("Bob", dto.getFirstName());
        assertEquals("Johnson", dto.getLastName());
        assertEquals("081-111-1111", dto.getPhoneNumber());
        assertEquals("bob@example.com", dto.getEmail());
        assertEquals("Yearly", dto.getContractTypeName());
        assertEquals(signDate, dto.getSignDate());
        assertEquals(startDate, dto.getStartDate());
        assertEquals(endDate, dto.getEndDate());
        assertEquals(1, dto.getAssets().size());
        assertEquals(1, dto.getRequests().size());
    }

    @Test
    void testCustomConstructorWithoutRoomSizeAssetsRequests() {
        LocalDateTime signDate = LocalDateTime.of(2024, 3, 10, 9, 0);
        LocalDateTime startDate = LocalDateTime.of(2024, 4, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2024, 9, 30, 23, 59);

        RoomDetailDto dto = new RoomDetailDto(
                300L,
                "C303",
                7,
                "Maintenance",
                "Charlie",
                "Lee",
                "082-222-2222",
                "charlie@example.com",
                "Short Term",
                signDate,
                startDate,
                endDate
        );

        assertEquals(300L, dto.getRoomId());
        assertEquals("C303", dto.getRoomNumber());
        assertEquals(7, dto.getRoomFloor());
        assertEquals("Maintenance", dto.getStatus());
        assertEquals("Charlie", dto.getFirstName());
        assertEquals("Lee", dto.getLastName());
        assertEquals("082-222-2222", dto.getPhoneNumber());
        assertEquals("charlie@example.com", dto.getEmail());
        assertEquals("Short Term", dto.getContractTypeName());
        assertEquals(signDate, dto.getSignDate());
        assertEquals(startDate, dto.getStartDate());
        assertEquals(endDate, dto.getEndDate());

        assertNull(dto.getRoomSize());
        assertNull(dto.getAssets());
        assertNull(dto.getRequests());
    }

    @Test
    void testEqualsAndHashCode() {
        LocalDateTime sign = LocalDateTime.of(2025, 1, 1, 10, 0);
        LocalDateTime start = LocalDateTime.of(2025, 2, 1, 12, 0);
        LocalDateTime end = LocalDateTime.of(2025, 12, 31, 18, 0);

        RoomDetailDto dto1 = new RoomDetailDto(
                400L, "D404", "2 Bedroom", 8, "Occupied",
                "Dana", "Kim", "083-333-3333", "dana@example.com",
                "Premium", sign, start, end, null, null
        );

        RoomDetailDto dto2 = new RoomDetailDto(
                400L, "D404", "2 Bedroom", 8, "Occupied",
                "Dana", "Kim", "083-333-3333", "dana@example.com",
                "Premium", sign, start, end, null, null
        );

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());

        dto2.setRoomNumber("Changed");
        assertNotEquals(dto1, dto2);
    }

    @Test
    void testToString() {
        RoomDetailDto dto = new RoomDetailDto();
        dto.setRoomId(500L);
        dto.setRoomNumber("E505");
        dto.setStatus("Vacant");

        assertTrue(dto.toString().contains("E505"));
        assertTrue(dto.toString().contains("Vacant"));
    }

    @Test
    void testJsonSerialization() throws Exception {
        LocalDateTime sign = LocalDateTime.now();
        LocalDateTime start = sign.plusDays(1);
        LocalDateTime end = start.plusMonths(12);

        RoomDetailDto original = new RoomDetailDto(
                600L, "F606", "Studio", 9, "Occupied",
                "Frank", "Ng", "084-444-4444", "frank@example.com",
                "Standard", sign, start, end, null, null
        );

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        String json = mapper.writeValueAsString(original);
        RoomDetailDto cloned = mapper.readValue(json, RoomDetailDto.class);

        assertEquals(original, cloned);
    }
}
