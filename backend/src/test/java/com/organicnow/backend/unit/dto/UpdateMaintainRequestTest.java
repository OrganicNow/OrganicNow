package com.organicnow.backend.unit.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.organicnow.backend.dto.UpdateMaintainRequest;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UpdateMaintainRequestTest {

    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void testGetterAndSetter() {
        UpdateMaintainRequest dto = new UpdateMaintainRequest();

        LocalDateTime schedule = LocalDateTime.now();
        LocalDateTime finish = schedule.plusDays(1);

        dto.setTargetType(1);
        dto.setRoomId(10L);
        dto.setRoomNumber("302");
        dto.setRoomAssetId(99L);

        dto.setIssueCategory(2);
        dto.setIssueTitle("Water leak");
        dto.setIssueDescription("Pipe broken");

        dto.setScheduledDate(schedule);
        dto.setFinishDate(finish);

        dto.setMaintainType("Electrical");
        dto.setTechnicianName("John Doe");
        dto.setTechnicianPhone("0812345678");
        dto.setWorkImageUrl("http://example.com/image.jpg");

        assertEquals(1, dto.getTargetType());
        assertEquals(10L, dto.getRoomId());
        assertEquals("302", dto.getRoomNumber());
        assertEquals(99L, dto.getRoomAssetId());

        assertEquals(2, dto.getIssueCategory());
        assertEquals("Water leak", dto.getIssueTitle());
        assertEquals("Pipe broken", dto.getIssueDescription());

        assertEquals(schedule, dto.getScheduledDate());
        assertEquals(finish, dto.getFinishDate());

        assertEquals("Electrical", dto.getMaintainType());
        assertEquals("John Doe", dto.getTechnicianName());
        assertEquals("0812345678", dto.getTechnicianPhone());
        assertEquals("http://example.com/image.jpg", dto.getWorkImageUrl());
    }

    @Test
    void testAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();

        UpdateMaintainRequest dto = new UpdateMaintainRequest(
                1, 5L, "502", 88L,
                4, "Air issue", "AC broken",
                now, now.plusHours(3),
                "AirConditioning", "Mike", "0811111111", "http://example.com/photo.png"
        );

        assertEquals(1, dto.getTargetType());
        assertEquals(5L, dto.getRoomId());
        assertEquals("502", dto.getRoomNumber());
        assertEquals(88L, dto.getRoomAssetId());

        assertEquals(4, dto.getIssueCategory());
        assertEquals("Air issue", dto.getIssueTitle());
        assertEquals("AC broken", dto.getIssueDescription());

        assertEquals(now, dto.getScheduledDate());
        assertEquals(now.plusHours(3), dto.getFinishDate());

        assertEquals("AirConditioning", dto.getMaintainType());
        assertEquals("Mike", dto.getTechnicianName());
        assertEquals("0811111111", dto.getTechnicianPhone());
        assertEquals("http://example.com/photo.png", dto.getWorkImageUrl());
    }

    @Test
    void testBuilder() {
        LocalDateTime now = LocalDateTime.now();

        UpdateMaintainRequest dto = UpdateMaintainRequest.builder()
                .targetType(3)
                .roomId(20L)
                .roomNumber("1201")
                .issueTitle("Light broken")
                .issueCategory(7)
                .scheduledDate(now)
                .maintainType("Electric")
                .technicianName("Krit")
                .technicianPhone("0890001111")
                .workImageUrl("http://img.com/test.jpg")
                .build();

        assertEquals(3, dto.getTargetType());
        assertEquals(20L, dto.getRoomId());
        assertEquals("1201", dto.getRoomNumber());
        assertEquals("Light broken", dto.getIssueTitle());
        assertEquals(7, dto.getIssueCategory());
        assertEquals(now, dto.getScheduledDate());
        assertEquals("Electric", dto.getMaintainType());
        assertEquals("Krit", dto.getTechnicianName());
        assertEquals("0890001111", dto.getTechnicianPhone());
        assertEquals("http://img.com/test.jpg", dto.getWorkImageUrl());
    }

    @Test
    void testJsonSerialization() throws Exception {
        UpdateMaintainRequest dto = UpdateMaintainRequest.builder()
                .issueTitle("Door stuck")
                .targetType(1)
                .technicianPhone("0800000000")
                .build();

        String json = mapper.writeValueAsString(dto);
        assertNotNull(json);
        assertTrue(json.contains("Door stuck"));
    }

    @Test
    void testJsonDeserialization() throws Exception {
        String json = """
        {
          "issueTitle": "Fan not working",
          "targetType": 2,
          "technicianName": "Somchai",
          "roomId": 77
        }
        """;

        UpdateMaintainRequest dto = mapper.readValue(json, UpdateMaintainRequest.class);

        assertEquals("Fan not working", dto.getIssueTitle());
        assertEquals(2, dto.getTargetType());
        assertEquals("Somchai", dto.getTechnicianName());
        assertEquals(77L, dto.getRoomId());
    }

    @Test
    void testUnknownJsonFieldsIgnored() throws Exception {
        String json = """
        {
          "issueTitle": "Leak",
          "unknownFieldA": "AAA",
          "unknownFieldB": 999,
          "technicianPhone": "0812345678"
        }
        """;

        UpdateMaintainRequest dto = mapper.readValue(json, UpdateMaintainRequest.class);

        assertEquals("Leak", dto.getIssueTitle());
        assertEquals("0812345678", dto.getTechnicianPhone());
    }

    @Test
    void testToStringNotEmpty() {
        UpdateMaintainRequest dto = new UpdateMaintainRequest();
        String ts = dto.toString();

        assertNotNull(ts);
        assertFalse(ts.isBlank());
    }
}
