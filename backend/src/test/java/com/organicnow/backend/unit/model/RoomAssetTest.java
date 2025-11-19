package com.organicnow.backend.unit.model;

import com.organicnow.backend.model.Asset;
import com.organicnow.backend.model.Room;
import com.organicnow.backend.model.RoomAsset;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoomAssetTest {

    @Test
    void testGetterAndSetter() {
        Asset asset = new Asset();
        asset.setId(1L);
        asset.setAssetName("Air Conditioner");

        Room room = new Room();
        room.setId(2L);
        room.setRoomNumber("B101");

        RoomAsset roomAsset = new RoomAsset();
        roomAsset.setId(100L);
        roomAsset.setAsset(asset);
        roomAsset.setRoom(room);

        assertEquals(100L, roomAsset.getId());
        assertEquals(1L, roomAsset.getAsset().getId());
        assertEquals("Air Conditioner", roomAsset.getAsset().getAssetName());
        assertEquals(2L, roomAsset.getRoom().getId());
        assertEquals("B101", roomAsset.getRoom().getRoomNumber());
    }

    @Test
    void testBuilderCreatesCorrectObject() {
        Asset asset = new Asset();
        asset.setId(3L);
        asset.setAssetName("Ceiling Fan");

        Room room = new Room();
        room.setId(4L);
        room.setRoomNumber("C202");

        RoomAsset roomAsset = RoomAsset.builder()
                .id(200L)
                .asset(asset)
                .room(room)
                .build();

        assertNotNull(roomAsset);
        assertEquals(200L, roomAsset.getId());
        assertEquals(3L, roomAsset.getAsset().getId());
        assertEquals(4L, roomAsset.getRoom().getId());
        assertEquals("Ceiling Fan", roomAsset.getAsset().getAssetName());
    }

    @Test
    void testAllArgsConstructor() {
        Asset asset = new Asset();
        asset.setId(5L);
        Room room = new Room();
        room.setId(6L);

        RoomAsset roomAsset = new RoomAsset(300L, asset, room);

        assertEquals(300L, roomAsset.getId());
        assertEquals(5L, roomAsset.getAsset().getId());
        assertEquals(6L, roomAsset.getRoom().getId());
    }

    @Test
    void testToStringNotNull() {
        RoomAsset roomAsset = new RoomAsset();
        roomAsset.setId(400L);
        assertNotNull(roomAsset.toString());
    }

    @Test
    void testEqualsReferenceOnly() {
        RoomAsset r1 = new RoomAsset();
        RoomAsset r2 = new RoomAsset();

        assertSame(r1, r1);   // อ้างอิงเดียวกัน
        assertNotSame(r1, r2); // คนละอ้างอิง
    }
}
