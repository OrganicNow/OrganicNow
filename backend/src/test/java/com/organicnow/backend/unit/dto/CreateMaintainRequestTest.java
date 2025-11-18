package com.organicnow.backend.unit.dto;

import com.organicnow.backend.dto.CreateMaintainRequest;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CreateMaintainRequestTest {

    // ============================================================
    // 1) Default constructor + setters/getters
    // ============================================================
    @Test
    void testDefaultConstructorAndSetters() {
        CreateMaintainRequest dto = new CreateMaintainRequest();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime schedule = now.plusDays(1);
        LocalDateTime finish = now.plusDays(3);

        dto.setTargetType(1);
        dto.setRoomId(11L);
        dto.setRoomNumber("201A");
        dto.setRoomAssetId(55L);
        dto.setIssueCategory(4);
        dto.setIssueTitle("Aircon not cooling");
        dto.setIssueDescription("Fan works but no cold air.");
        dto.setCreateDate(now);
        dto.setScheduledDate(schedule);
        dto.setFinishDate(finish);

        dto.setMaintainType("Electrical");
        dto.setTechnicianName("Somchai");
        dto.setTechnicianPhone("0812345678");

        assertEquals(1, dto.getTargetType());
        assertEquals(11L, dto.getRoomId());
        assertEquals("201A", dto.getRoomNumber());
        assertEquals(55L, dto.getRoomAssetId());
        assertEquals(4, dto.getIssueCategory());
        assertEquals("Aircon not cooling", dto.getIssueTitle());
        assertEquals("Fan works but no cold air.", dto.getIssueDescription());
        assertEquals(now, dto.getCreateDate());
        assertEquals(schedule, dto.getScheduledDate());
        assertEquals(finish, dto.getFinishDate());

        assertEquals("Electrical", dto.getMaintainType());
        assertEquals("Somchai", dto.getTechnicianName());
        assertEquals("0812345678", dto.getTechnicianPhone());
    }

    // ============================================================
    // 2) AllArgsConstructor
    // ============================================================
    @Test
    void testAllArgsConstructor() {
        LocalDateTime create = LocalDateTime.now();
        LocalDateTime schedule = create.plusDays(2);
        LocalDateTime finish = create.plusDays(5);

        CreateMaintainRequest dto = new CreateMaintainRequest(
                2,
                99L,
                "503",
                100L,
                1,
                "Water leak",
                "Leak from the ceiling",
                create,
                schedule,
                finish,
                "Plumbing",
                "Anan",
                "0890001111"
        );

        assertEquals(2, dto.getTargetType());
        assertEquals(99L, dto.getRoomId());
        assertEquals("503", dto.getRoomNumber());
        assertEquals(100L, dto.getRoomAssetId());
        assertEquals(1, dto.getIssueCategory());
        assertEquals("Water leak", dto.getIssueTitle());
        assertEquals("Leak from the ceiling", dto.getIssueDescription());
        assertEquals(create, dto.getCreateDate());
        assertEquals(schedule, dto.getScheduledDate());
        assertEquals(finish, dto.getFinishDate());

        assertEquals("Plumbing", dto.getMaintainType());
        assertEquals("Anan", dto.getTechnicianName());
        assertEquals("0890001111", dto.getTechnicianPhone());
    }

    // ============================================================
    // 3) Builder test
    // ============================================================
    @Test
    void testBuilder() {
        LocalDateTime create = LocalDateTime.now();

        CreateMaintainRequest dto = CreateMaintainRequest.builder()
                .targetType(1)
                .roomId(20L)
                .roomNumber("701B")
                .issueCategory(3)
                .issueTitle("Light broken")
                .createDate(create)
                .maintainType("Electrical")
                .technicianName("Preecha")
                .technicianPhone("0801234567")
                .build();

        assertEquals(1, dto.getTargetType());
        assertEquals(20L, dto.getRoomId());
        assertEquals("701B", dto.getRoomNumber());
        assertEquals(3, dto.getIssueCategory());
        assertEquals("Light broken", dto.getIssueTitle());
        assertEquals(create, dto.getCreateDate());
        assertEquals("Electrical", dto.getMaintainType());
        assertEquals("Preecha", dto.getTechnicianName());
        assertEquals("0801234567", dto.getTechnicianPhone());
    }

    // ============================================================
    // 4) Null Safety test
    // ============================================================
    @Test
    void testNullValues() {
        CreateMaintainRequest dto = new CreateMaintainRequest();

        dto.setRoomId(null);
        dto.setRoomNumber(null);
        dto.setIssueDescription(null);
        dto.setCreateDate(null);
        dto.setScheduledDate(null);
        dto.setFinishDate(null);
        dto.setMaintainType(null);
        dto.setTechnicianName(null);
        dto.setTechnicianPhone(null);

        assertNull(dto.getRoomId());
        assertNull(dto.getRoomNumber());
        assertNull(dto.getIssueDescription());
        assertNull(dto.getCreateDate());
        assertNull(dto.getScheduledDate());
        assertNull(dto.getFinishDate());
        assertNull(dto.getMaintainType());
        assertNull(dto.getTechnicianName());
        assertNull(dto.getTechnicianPhone());
    }

    // ============================================================
    // 5) toString() should not be null
    // ============================================================
    @Test
    void testToString() {
        CreateMaintainRequest dto = new CreateMaintainRequest();
        assertNotNull(dto.toString());
    }
}
