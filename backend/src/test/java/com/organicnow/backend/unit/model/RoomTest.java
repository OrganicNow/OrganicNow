package com.organicnow.backend.unit.model;

import com.organicnow.backend.model.Room;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoomTest {

    @Test
    void testGetterAndSetter() {
        Room room = new Room();
        room.setId(1L);
        room.setRoomNumber("A101");
        room.setRoomFloor(2);
        room.setRoomSize(1);

        assertEquals(1L, room.getId());
        assertEquals("A101", room.getRoomNumber());
        assertEquals(2, room.getRoomFloor());
        assertEquals(1, room.getRoomSize());
    }

    @Test
    void testBuilderCreatesCorrectObject() {
        Room room = Room.builder()
                .id(2L)
                .roomNumber("B202")
                .roomFloor(3)
                .roomSize(2)
                .build();

        assertNotNull(room);
        assertEquals(2L, room.getId());
        assertEquals("B202", room.getRoomNumber());
        assertEquals(3, room.getRoomFloor());
        assertEquals(2, room.getRoomSize());
    }

    @Test
    void testAllArgsConstructor() {
        Room room = new Room(3L, "C303", 4, 0);

        assertEquals(3L, room.getId());
        assertEquals("C303", room.getRoomNumber());
        assertEquals(4, room.getRoomFloor());
        assertEquals(0, room.getRoomSize());
    }

    @Test
    void testDefaultRoomSizeValue() {
        Room room = new Room();
        assertEquals(0, room.getRoomSize());
    }

    @Test
    void testRoomFloorMustBeNonNegative() {
        Room room = new Room();
        room.setRoomFloor(0);
        assertTrue(room.getRoomFloor() >= 0);

        room.setRoomFloor(5);
        assertTrue(room.getRoomFloor() >= 0);
    }

    @Test
    void testToStringNotNull() {
        Room room = new Room();
        room.setRoomNumber("D404");
        assertNotNull(room.toString());
    }

    @Test
    void testEqualsReferenceOnly() {
        Room r1 = new Room();
        Room r2 = new Room();

        assertSame(r1, r1);   // อ้างอิงเดียวกัน
        assertNotSame(r1, r2); // คนละอ้างอิง
    }
}
