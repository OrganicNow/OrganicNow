package com.organicnow.backend.unit.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.organicnow.backend.dto.MaintainDto;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.DeserializationFeature;


import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class MaintainDtoTest {

    private ObjectMapper mapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // ⭐ FIX: ignore unknown fields like "state"
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return mapper;
    }

    @Test
    void testBuilderAndFields() {
        LocalDateTime now = LocalDateTime.of(2024, 1, 1, 10, 0);

        MaintainDto dto = MaintainDto.builder()
                .id(10L)
                .targetType(1)
                .roomId(20L)
                .roomNumber("A301")
                .roomFloor(3)
                .roomAssetId(300L)
                .issueCategory(2)
                .issueTitle("Air leak")
                .issueDescription("Water leak from AC")
                .createDate(now)
                .scheduledDate(now.plusDays(1))
                .finishDate(now.plusDays(2))
                .maintainType("Electrical")
                .technicianName("Somchai")
                .technicianPhone("0812345678")
                .workImageUrl("http://example.com/image.jpg")
                .build();

        assertEquals(10L, dto.getId());
        assertEquals(1, dto.getTargetType());
        assertEquals(20L, dto.getRoomId());
        assertEquals("A301", dto.getRoomNumber());
        assertEquals(3, dto.getRoomFloor());
        assertEquals(300L, dto.getRoomAssetId());
        assertEquals(2, dto.getIssueCategory());
        assertEquals("Air leak", dto.getIssueTitle());
        assertEquals("Water leak from AC", dto.getIssueDescription());
        assertEquals(now, dto.getCreateDate());
        assertEquals(now.plusDays(1), dto.getScheduledDate());
        assertEquals(now.plusDays(2), dto.getFinishDate());
        assertEquals("Electrical", dto.getMaintainType());
        assertEquals("Somchai", dto.getTechnicianName());
        assertEquals("0812345678", dto.getTechnicianPhone());
        assertEquals("http://example.com/image.jpg", dto.getWorkImageUrl());
    }

    @Test
    void testGettersAndSetters() {
        MaintainDto dto = new MaintainDto();

        dto.setId(1L);
        dto.setTargetType(0);
        dto.setRoomId(100L);
        dto.setRoomNumber("B204");
        dto.setRoomFloor(2);
        dto.setRoomAssetId(999L);
        dto.setIssueCategory(1);
        dto.setIssueTitle("Broken pipe");
        dto.setIssueDescription("Pipe leaking in wall");

        LocalDateTime now = LocalDateTime.now();
        dto.setCreateDate(now);
        dto.setScheduledDate(now.plusHours(2));
        dto.setFinishDate(now.plusDays(1));

        dto.setMaintainType("Plumbing");
        dto.setTechnicianName("Krit");
        dto.setTechnicianPhone("0800000000");
        dto.setWorkImageUrl("http://x.com/y.jpg");

        assertEquals(1L, dto.getId());
        assertEquals(0, dto.getTargetType());
        assertEquals(100L, dto.getRoomId());
        assertEquals("B204", dto.getRoomNumber());
        assertEquals(2, dto.getRoomFloor());
        assertEquals(999L, dto.getRoomAssetId());
        assertEquals(1, dto.getIssueCategory());
        assertEquals("Broken pipe", dto.getIssueTitle());
        assertEquals("Pipe leaking in wall", dto.getIssueDescription());
        assertEquals(now, dto.getCreateDate());
        assertEquals(now.plusHours(2), dto.getScheduledDate());
        assertEquals(now.plusDays(1), dto.getFinishDate());
        assertEquals("Plumbing", dto.getMaintainType());
        assertEquals("Krit", dto.getTechnicianName());
        assertEquals("0800000000", dto.getTechnicianPhone());
        assertEquals("http://x.com/y.jpg", dto.getWorkImageUrl());
    }

    @Test
    void testNullFields() {
        MaintainDto dto = new MaintainDto();
        assertNull(dto.getId());
        assertNull(dto.getTargetType());
        assertNull(dto.getRoomId());
        assertNull(dto.getRoomNumber());
        assertNull(dto.getRoomFloor());
        assertNull(dto.getRoomAssetId());
        assertNull(dto.getIssueCategory());
        assertNull(dto.getIssueTitle());
        assertNull(dto.getIssueDescription());
        assertNull(dto.getCreateDate());
        assertNull(dto.getScheduledDate());
        assertNull(dto.getFinishDate());
        assertNull(dto.getMaintainType());
        assertNull(dto.getTechnicianName());
        assertNull(dto.getTechnicianPhone());
        assertNull(dto.getWorkImageUrl());
    }

    @Test
    void testState_NotStarted() {
        MaintainDto dto = new MaintainDto();
        dto.setFinishDate(null);
        assertEquals("Not Started", dto.getState());
    }

    @Test
    void testState_Complete() {
        MaintainDto dto = new MaintainDto();
        dto.setFinishDate(LocalDateTime.now());
        assertEquals("Complete", dto.getState());
    }

    @Test
    void testState_FutureFinish() {
        MaintainDto dto = new MaintainDto();
        dto.setFinishDate(LocalDateTime.now().plusDays(3));
        assertEquals("Complete", dto.getState());
    }

    @Test
    void testCloneViaJson() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        MaintainDto dto1 = MaintainDto.builder()
                .id(100L)
                .issueTitle("Test")
                .createDate(now)
                .build();

        MaintainDto dto2 =
                mapper().readValue(mapper().writeValueAsString(dto1), MaintainDto.class);

        assertEquals(dto1.getId(), dto2.getId());
        assertEquals(dto1.getIssueTitle(), dto2.getIssueTitle());
        assertEquals(dto1.getCreateDate(), dto2.getCreateDate());
    }

    @Test
    void testToStringDoesNotCrash() {
        MaintainDto dto = new MaintainDto();
        assertNotNull(dto.toString());
    }

    @Test
    void testFieldIndependence() {
        MaintainDto dto = new MaintainDto();
        dto.setIssueTitle("A");
        dto.setIssueDescription("B");

        assertEquals("A", dto.getIssueTitle());
        assertEquals("B", dto.getIssueDescription());

        dto.setIssueTitle("C");
        assertEquals("C", dto.getIssueTitle());
        assertEquals("B", dto.getIssueDescription());
    }
}
