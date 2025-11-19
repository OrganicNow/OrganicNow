package com.organicnow.backend.integration.service;

import com.organicnow.backend.model.*;
import com.organicnow.backend.repository.*;
import com.organicnow.backend.service.RoomService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(properties = {
        "spring.task.scheduling.enabled=false"
})
@Testcontainers
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class RoomServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17")
                    .withDatabaseName("testdb")
                    .withUsername("postgres")
                    .withPassword("postgres");

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired private RoomService roomService;

    @Autowired private RoomRepository roomRepository;
    @Autowired private AssetRepository assetRepository;
    @Autowired private RoomAssetRepository roomAssetRepository;
    @Autowired private AssetEventRepository assetEventRepository;
    @Autowired private MaintainRepository maintainRepository;

    @PersistenceContext
    private EntityManager em;

    private Room room;

    @BeforeEach
    void setup() {
        room = roomRepository.save(
                Room.builder()
                        .roomNumber("C101")
                        .roomFloor(1)
                        .roomSize(1)
                        .build()
        );
    }

    // ------------------------------------------------------------------------
    private AssetGroup createGroup() {
        AssetGroup g = AssetGroup.builder()
                .assetGroupName("Furniture")
                .freeReplacement(true)
                .monthlyAddonFee(BigDecimal.ZERO)
                .oneTimeDamageFee(BigDecimal.ZERO)
                .build();
        em.persist(g);
        return g;
    }

    // ------------------------------------------------------------------------
    @Test
    void testAddAssetToRoom() {

        AssetGroup group = createGroup();

        Asset asset = assetRepository.save(
                Asset.builder()
                        .assetName("Chair")
                        .status("available")
                        .assetGroup(group)
                        .build()
        );

        roomService.addAssetToRoom(room.getId(), asset.getId());

        List<RoomAsset> relations = roomAssetRepository.findByRoomId(room.getId());
        assertThat(relations).hasSize(1);
        assertThat(relations.get(0).getAsset().getAssetName()).isEqualTo("Chair");

        Asset updated = assetRepository.findById(asset.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo("in_use");
    }

    // ------------------------------------------------------------------------
    @Test
    void testRemoveAssetFromRoom() {

        AssetGroup group = createGroup();

        Asset asset = assetRepository.save(
                Asset.builder()
                        .assetName("Table")
                        .status("available")
                        .assetGroup(group)
                        .build()
        );

        roomService.addAssetToRoom(room.getId(), asset.getId());
        roomService.removeAssetFromRoom(room.getId(), asset.getId());

        assertThat(roomAssetRepository.findByRoomId(room.getId())).isEmpty();

        Asset updated = assetRepository.findById(asset.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo("available");
    }

    // ------------------------------------------------------------------------
    @Test
    void testUpdateRoomAssets() {

        AssetGroup group = createGroup();

        Asset a1 = assetRepository.save(Asset.builder().assetName("Chair").status("available").assetGroup(group).build());
        Asset a2 = assetRepository.save(Asset.builder().assetName("Table").status("available").assetGroup(group).build());
        Asset a3 = assetRepository.save(Asset.builder().assetName("Lamp").status("available").assetGroup(group).build());

        // initial: add a1 + a2
        roomService.updateRoomAssets(room.getId(), List.of(a1.getId(), a2.getId()));

        assertThat(roomAssetRepository.findByRoomId(room.getId())).hasSize(2);

        // update: remove a1, add a3
        roomService.updateRoomAssets(room.getId(), List.of(a2.getId(), a3.getId()));

        List<RoomAsset> result = roomAssetRepository.findByRoomId(room.getId());
        assertThat(result).hasSize(2);

        assertThat(result.stream().map(ra -> ra.getAsset().getAssetName()))
                .containsExactlyInAnyOrder("Table", "Lamp");

        assertThat(assetRepository.findById(a1.getId()).orElseThrow().getStatus()).isEqualTo("available");
        assertThat(assetRepository.findById(a2.getId()).orElseThrow().getStatus()).isEqualTo("in_use");
        assertThat(assetRepository.findById(a3.getId()).orElseThrow().getStatus()).isEqualTo("in_use");
    }

    // ------------------------------------------------------------------------
    @Test
    void testUpdateRoomAssetsWithReason() {

        AssetGroup group = createGroup();

        Asset a1 = assetRepository.save(
                Asset.builder()
                        .assetName("Fan")
                        .status("available")
                        .assetGroup(group)
                        .build()
        );

        Asset a2 = assetRepository.save(
                Asset.builder()
                        .assetName("TV")
                        .status("available")
                        .assetGroup(group)
                        .build()
        );

        // Add a1
        roomService.updateRoomAssetsWithReason(room.getId(), List.of(a1.getId()), "install", "Initial");

        // Replace with a2
        roomService.updateRoomAssetsWithReason(room.getId(), List.of(a2.getId()), "replace", "Broken");

        // ----------------------------
        // ✅ Validate final room assets
        // ----------------------------
        List<RoomAsset> list = roomAssetRepository.findByRoomId(room.getId());
        assertThat(list).hasSize(1);
        assertThat(list.get(0).getAsset().getAssetName()).isEqualTo("TV");

        // ----------------------------
        // ✅ Validate asset events
        // ----------------------------
        List<AssetEvent> events = assetEventRepository.findByRoom_Id(room.getId());

        // ระบบจริงสร้าง 3 events:
        // 1. added (Fan)
        // 2. removed (Fan)
        // 3. added (TV)
        assertThat(events).hasSize(3);

        long addedCount = events.stream()
                .filter(e -> "added".equals(e.getEventType()))
                .count();

        long removedCount = events.stream()
                .filter(e -> "removed".equals(e.getEventType()))
                .count();

        assertThat(addedCount).isEqualTo(2);
        assertThat(removedCount).isEqualTo(1);

        // ----------------------------
        // ตรวจความถูกต้องของ reason และ note
        // ----------------------------
        assertThat(events.stream()
                .map(AssetEvent::getReasonType))
                .contains("install", "replace");

        assertThat(events.stream()
                .map(AssetEvent::getNote))
                .contains("Initial", "Broken");
    }

}
