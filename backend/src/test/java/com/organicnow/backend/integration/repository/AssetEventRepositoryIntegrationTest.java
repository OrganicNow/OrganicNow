package com.organicnow.backend.integration.repository;

import com.organicnow.backend.model.Asset;
import com.organicnow.backend.model.AssetEvent;
import com.organicnow.backend.model.AssetGroup;
import com.organicnow.backend.model.Room;
import com.organicnow.backend.repository.AssetEventRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Transactional   // ✅ ให้แต่ละ test อยู่ใน transaction เดียวและ rollback ให้อัตโนมัติ
class AssetEventRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private AssetEventRepository assetEventRepository;

    @Autowired
    private EntityManager entityManager;

    // ------------------------------------------------------------------------
    // Helper methods
    // ------------------------------------------------------------------------

    private Room persistRoom(String roomNumber, int floor, int size) {
        Room room = Room.builder()
                .roomNumber(roomNumber)
                .roomFloor(floor)
                .roomSize(size)
                .build();

        entityManager.persist(room);
        entityManager.flush();
        return room;
    }

    private AssetGroup persistAssetGroup(String name) {
        AssetGroup group = new AssetGroup();
        group.setAssetGroupName(name);
        // field อื่น ๆ เป็น nullable เลยไม่จำเป็นต้องเซ็ตก็ได้
        entityManager.persist(group);
        entityManager.flush();
        return group;
    }

    private Asset persistAsset(AssetGroup group, String assetName) {
        Asset asset = new Asset();
        asset.setAssetGroup(group);        // NOT NULL
        asset.setAssetName(assetName);     // @NotBlank
        asset.setStatus("ACTIVE");         // @NotBlank

        entityManager.persist(asset);
        entityManager.flush();
        return asset;
    }

    private AssetEvent persistEvent(Room room, Asset asset) {
        AssetEvent event = new AssetEvent();
        event.setRoom(room);
        event.setAsset(asset);

        // TODO: ถ้า eventType เป็น enum ให้เปลี่ยนค่าตรงนี้ให้ตรง enum จริง
        event.setEventType("ASSIGNED");
        event.setCreatedAt(LocalDateTime.now());
        // reasonType / note เป็น nullable

        entityManager.persist(event);
        entityManager.flush();
        return event;
    }

    // ------------------------------------------------------------------------
    // TEST #1: findByRoom_Id()
    // ------------------------------------------------------------------------

    @Test
    @DisplayName("findByRoom_Id() ควรคืน event เฉพาะของ room ที่ระบุ")
    void findByRoomId_shouldReturnEventsForThatRoomOnly() {
        // given
        Room roomA = persistRoom("A101", 1, 0);
        Room roomB = persistRoom("B202", 2, 1);

        AssetGroup group = persistAssetGroup("Group-findBy");
        Asset asset = persistAsset(group, "Asset-1");

        persistEvent(roomA, asset);
        persistEvent(roomA, asset);
        persistEvent(roomB, asset);

        // when
        List<AssetEvent> results =
                assetEventRepository.findByRoom_Id(roomA.getId());

        // then
        assertThat(results)
                .hasSize(2)
                .extracting(e -> e.getRoom().getId())
                .containsOnly(roomA.getId());
    }

    // ------------------------------------------------------------------------
    // TEST #2: deleteByRoom_Id()
    // ------------------------------------------------------------------------

    @Test
    @DisplayName("deleteByRoom_Id() ควรลบ event เฉพาะของ room นั้น ๆ")
    void deleteByRoomId_shouldDeleteOnlyThatRoomsEvents() {
        // given
        Room roomA = persistRoom("A101", 1, 0);
        Room roomB = persistRoom("B202", 2, 1);

        AssetGroup group = persistAssetGroup("Group-deleteBy");
        Asset asset = persistAsset(group, "Asset-1");

        persistEvent(roomA, asset);
        persistEvent(roomA, asset);
        persistEvent(roomB, asset);

        // when
        assetEventRepository.deleteByRoom_Id(roomA.getId());

        // then
        List<AssetEvent> remaining = assetEventRepository.findAll();

        assertThat(remaining)
                .hasSize(1)
                .extracting(e -> e.getRoom().getId())
                .containsOnly(roomB.getId());
    }
}
