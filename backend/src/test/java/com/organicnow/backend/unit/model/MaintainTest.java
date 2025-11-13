package com.organicnow.backend.unit.model;

import com.organicnow.backend.model.Maintain;
import com.organicnow.backend.model.Room;
import com.organicnow.backend.model.RoomAsset;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class MaintainTest {

    @Test
    void testGetterAndSetter() {
        Room room = new Room();
        room.setId(1L);

        RoomAsset asset = new RoomAsset();
        asset.setId(2L);

        Maintain maintain = new Maintain();
        maintain.setId(100L);
        maintain.setTargetType(1);
        maintain.setRoom(room);
        maintain.setRoomAsset(asset);
        maintain.setIssueCategory(3);
        maintain.setIssueTitle("Broken Chair");
        maintain.setIssueDescription("Chair leg is broken");
        maintain.setCreateDate(LocalDateTime.of(2025, 1, 1, 10, 0));
        maintain.setScheduledDate(LocalDateTime.of(2025, 1, 3, 14, 0));
        maintain.setFinishDate(LocalDateTime.of(2025, 1, 4, 16, 0));
        maintain.setMaintainType("Replace");
        maintain.setTechnicianName("John Doe");
        maintain.setTechnicianPhone("0812345678");
        maintain.setWorkImageUrl("http://example.com/work/1.jpg");

        assertEquals(100L, maintain.getId());
        assertEquals(1, maintain.getTargetType());
        assertEquals(1L, maintain.getRoom().getId());
        assertEquals(2L, maintain.getRoomAsset().getId());
        assertEquals(3, maintain.getIssueCategory());
        assertEquals("Broken Chair", maintain.getIssueTitle());
        assertEquals("Chair leg is broken", maintain.getIssueDescription());
        assertEquals("Replace", maintain.getMaintainType());
        assertEquals("John Doe", maintain.getTechnicianName());
        assertEquals("0812345678", maintain.getTechnicianPhone());
        assertEquals("http://example.com/work/1.jpg", maintain.getWorkImageUrl());
    }

    @Test
    void testBuilderCreatesCorrectObject() {
        Room room = new Room();
        room.setId(10L);

        Maintain maintain = Maintain.builder()
                .id(101L)
                .targetType(0)
                .room(room)
                .issueCategory(2)
                .issueTitle("Water Leak")
                .issueDescription("Pipe leaking near bathroom")
                .maintainType("Fix")
                .technicianName("Jane Smith")
                .technicianPhone("0899999999")
                .createDate(LocalDateTime.of(2025, 2, 1, 10, 0))
                .scheduledDate(LocalDateTime.of(2025, 2, 2, 12, 0))
                .finishDate(LocalDateTime.of(2025, 2, 3, 16, 0))
                .workImageUrl("http://example.com/work/2.jpg")
                .build();

        assertNotNull(maintain);
        assertEquals(101L, maintain.getId());
        assertEquals(0, maintain.getTargetType());
        assertEquals(10L, maintain.getRoom().getId());
        assertEquals("Water Leak", maintain.getIssueTitle());
        assertEquals("Pipe leaking near bathroom", maintain.getIssueDescription());
        assertEquals("Fix", maintain.getMaintainType());
        assertEquals("Jane Smith", maintain.getTechnicianName());
        assertEquals("0899999999", maintain.getTechnicianPhone());
        assertEquals("http://example.com/work/2.jpg", maintain.getWorkImageUrl());
    }

    @Test
    void testAllArgsConstructor() {
        Room room = new Room();
        room.setId(5L);
        RoomAsset asset = new RoomAsset();
        asset.setId(8L);

        LocalDateTime now = LocalDateTime.of(2025, 3, 1, 9, 0);

        Maintain maintain = new Maintain(
                202L,
                1,
                room,
                asset,
                4,
                "Electrical Short",
                "Wiring problem in wall",
                now,
                now.plusDays(1),
                now.plusDays(2),
                "Maintenance",
                "Alex Tech",
                "0800000000",
                "http://example.com/work/3.jpg"
        );

        assertEquals(202L, maintain.getId());
        assertEquals(1, maintain.getTargetType());
        assertEquals(5L, maintain.getRoom().getId());
        assertEquals(8L, maintain.getRoomAsset().getId());
        assertEquals(4, maintain.getIssueCategory());
        assertEquals("Electrical Short", maintain.getIssueTitle());
        assertEquals("Wiring problem in wall", maintain.getIssueDescription());
        assertEquals("Maintenance", maintain.getMaintainType());
        assertEquals("Alex Tech", maintain.getTechnicianName());
        assertEquals("0800000000", maintain.getTechnicianPhone());
        assertEquals("http://example.com/work/3.jpg", maintain.getWorkImageUrl());
    }

    @Test
    void testOptionalRoomAssetCanBeNull() {
        Room room = new Room();
        room.setId(11L);

        Maintain maintain = new Maintain();
        maintain.setRoom(room);
        maintain.setRoomAsset(null); // optional

        assertNotNull(maintain.getRoom());
        assertNull(maintain.getRoomAsset());
    }

    @Test
    void testToStringNotNull() {
        Maintain maintain = new Maintain();
        maintain.setIssueTitle("Air Conditioner Issue");
        assertNotNull(maintain.toString());
    }

    @Test
    void testEqualsReferenceOnly() {
        Maintain m1 = new Maintain();
        Maintain m3 = new Maintain();

        assertSame(m1, m1);   // อ้างอิงเดียวกัน
        assertNotSame(m1, m3); // คนละอ้างอิง
    }
}
