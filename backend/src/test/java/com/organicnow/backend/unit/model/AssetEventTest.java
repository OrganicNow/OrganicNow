package com.organicnow.backend.unit.model;

import com.organicnow.backend.model.Asset;
import com.organicnow.backend.model.AssetEvent;
import com.organicnow.backend.model.Room;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AssetEventTest {

    @Test
    void testGetterAndSetter() {
        AssetEvent event = new AssetEvent();
        event.setEventId(1L);
        event.setEventType("added");
        event.setReasonType("addon");
        event.setNote("Installed new TV");

        assertEquals(1L, event.getEventId());
        assertEquals("added", event.getEventType());
        assertEquals("addon", event.getReasonType());
        assertEquals("Installed new TV", event.getNote());
    }

    @Test
    void testRelationshipWithRoomAndAsset() {
        Room room = new Room();
        room.setId(10L);

        Asset asset = new Asset();
        asset.setId(20L);
        asset.setAssetName("Air Conditioner");

        AssetEvent event = new AssetEvent();
        event.setRoom(room);
        event.setAsset(asset);

        assertNotNull(event.getRoom());
        assertNotNull(event.getAsset());
        assertEquals(10L, event.getRoom().getId());
        assertEquals(20L, event.getAsset().getId());
        assertEquals("Air Conditioner", event.getAsset().getAssetName());
    }

    @Test
    void testBuilderCreatesCorrectObject() {
        Room room = new Room();
        room.setId(5L);

        Asset asset = new Asset();
        asset.setId(7L);
        asset.setAssetName("Microwave");

        AssetEvent event = AssetEvent.builder()
                .eventId(2L)
                .room(room)
                .asset(asset)
                .eventType("removed")
                .reasonType("damage")
                .note("Broken door")
                .build();

        assertNotNull(event);
        assertEquals(2L, event.getEventId());
        assertEquals("removed", event.getEventType());
        assertEquals("damage", event.getReasonType());
        assertEquals("Broken door", event.getNote());
        assertEquals(5L, event.getRoom().getId());
        assertEquals(7L, event.getAsset().getId());
    }

    @Test
    void testAllArgsConstructor() {
        Room room = new Room();
        room.setId(15L);

        Asset asset = new Asset();
        asset.setId(25L);

        LocalDateTime now = LocalDateTime.of(2025, 11, 13, 10, 0);
        AssetEvent event = new AssetEvent(3L, room, asset, "added", "addon", "test note", now);

        assertEquals(3L, event.getEventId());
        assertEquals("added", event.getEventType());
        assertEquals("addon", event.getReasonType());
        assertEquals("test note", event.getNote());
        assertEquals(now, event.getCreatedAt());
    }

    @Test
    void testPrePersistSetsCreatedAt() throws Exception {
        AssetEvent event = new AssetEvent();
        assertNull(event.getCreatedAt());

        // เรียก protected method ผ่าน reflection
        var method = AssetEvent.class.getDeclaredMethod("onCreate");
        method.setAccessible(true);
        method.invoke(event);

        assertNotNull(event.getCreatedAt());
        assertTrue(event.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }


    @Test
    void testToStringNotNull() {
        AssetEvent event = new AssetEvent();
        event.setEventType("added");
        assertNotNull(event.toString());
    }

    @Test
    void testEqualsReferenceOnly() {
        AssetEvent e1 = new AssetEvent();
        AssetEvent e3 = new AssetEvent();

        assertEquals(e1, e1);   // อ้างอิงเดียวกัน
        assertNotEquals(e1, e3); // คนละอ้างอิง
    }
}
