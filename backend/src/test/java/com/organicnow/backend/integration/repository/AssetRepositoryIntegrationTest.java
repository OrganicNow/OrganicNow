package com.organicnow.backend.integration.repository;

import com.organicnow.backend.dto.AssetDto;
import com.organicnow.backend.model.Asset;
import com.organicnow.backend.model.AssetGroup;
import com.organicnow.backend.model.Room;
import com.organicnow.backend.model.RoomAsset;
import com.organicnow.backend.repository.AssetEventRepository;
import com.organicnow.backend.repository.AssetGroupRepository;
import com.organicnow.backend.repository.AssetRepository;
import com.organicnow.backend.repository.RoomAssetRepository;
import com.organicnow.backend.repository.RoomRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class AssetRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired private AssetEventRepository assetEventRepository;
    @Autowired private RoomAssetRepository roomAssetRepository;
    @Autowired private AssetRepository assetRepository;
    @Autowired private RoomRepository roomRepository;
    @Autowired private AssetGroupRepository assetGroupRepository;

    // -------------------------
    // Helper methods
    // -------------------------
    private Room createRoom(int floor, String number) {
        Room room = new Room();
        room.setRoomFloor(floor);
        room.setRoomNumber(number);
        room.setRoomSize(0);
        return roomRepository.save(room);
    }

    private AssetGroup createAssetGroup(String name, BigDecimal fee) {
        AssetGroup g = new AssetGroup();
        g.setAssetGroupName(name);
        g.setFreeReplacement(false);
        g.setMonthlyAddonFee(fee);
        g.setOneTimeDamageFee(BigDecimal.ZERO);
        return assetGroupRepository.save(g);
    }

    private Asset createAsset(String name, String status, AssetGroup group) {
        Asset a = new Asset();
        a.setAssetName(name);
        a.setStatus(status);
        a.setAssetGroup(group);
        return assetRepository.save(a);
    }

    private RoomAsset linkRoomAsset(Room room, Asset asset) {
        RoomAsset ra = new RoomAsset();
        ra.setRoom(room);
        ra.setAsset(asset);
        return roomAssetRepository.save(ra);
    }

    // --------------------------------------------------------
    // Tests — each test cleans its own data before setup
    // --------------------------------------------------------

    @Test
    @DisplayName("findAssetsByRoomId(): ดึงเฉพาะ asset ของห้องนั้น และไม่เอา status=deleted")
    void findAssetsByRoomId_shouldReturnOnlyThatRoomAssets_excludingDeleted() {

        // เคลียร์เฉพาะตารางที่เกี่ยวกับ asset (ไม่แตะ room จากเทสอื่น/ระบบ)
        assetEventRepository.deleteAll();
        roomAssetRepository.deleteAll();
        assetRepository.deleteAll();

        // given - ใช้ room_number ไม่ซ้ำกับเทสอื่น
        Room roomA = createRoom(1, "AR-T1-101");
        Room roomB = createRoom(2, "AR-T1-201");

        AssetGroup group = createAssetGroup("Electronics", new BigDecimal("100"));

        Asset tvA = createAsset("TV A", "in_use", group);
        Asset fridgeA = createAsset("Fridge A", "available", group);
        Asset deletedA = createAsset("Old Fan A", "deleted", group);
        Asset tvB = createAsset("TV B", "in_use", group);

        linkRoomAsset(roomA, tvA);
        linkRoomAsset(roomA, fridgeA);
        linkRoomAsset(roomA, deletedA);
        linkRoomAsset(roomB, tvB);

        // when
        List<AssetDto> results = assetRepository.findAssetsByRoomId(roomA.getId());

        // then
        assertThat(results)
                .hasSize(2)
                .extracting(AssetDto::getAssetName)
                .containsExactlyInAnyOrder("TV A", "Fridge A");
    }

    @Test
    @DisplayName("findAllAssetOptions(): ดึง asset ทั้งหมดที่ไม่ deleted พร้อม room ถ้ามี")
    void findAllAssetOptions_shouldReturnAllNonDeletedAssets() {

        assetEventRepository.deleteAll();
        roomAssetRepository.deleteAll();
        assetRepository.deleteAll();

        // room_number สำหรับเทสนี้อีกชุดนึง ไม่ชนเทสอื่น
        Room room1 = createRoom(1, "AR-T2-101");
        Room room2 = createRoom(2, "AR-T2-201");

        AssetGroup group = createAssetGroup("Furnitures", BigDecimal.ZERO);

        Asset chair1 = createAsset("Chair 1", "available", group);
        Asset chair2 = createAsset("Chair 2", "in_use", group);
        Asset sofa = createAsset("Sofa", "deleted", group);
        Asset table = createAsset("Table", "available", group);

        linkRoomAsset(room1, chair1);
        linkRoomAsset(room2, chair2);

        List<AssetDto> results = assetRepository.findAllAssetOptions();

        assertThat(results)
                .hasSize(3)
                .extracting(AssetDto::getAssetName)
                .containsExactlyInAnyOrder("Chair 1", "Chair 2", "Table");
    }

    @Test
    @DisplayName("findAvailableById(): คืน asset เฉพาะเมื่อ status=available เท่านั้น")
    void findAvailableById_shouldReturnOnlyAvailableAsset() {

        assetEventRepository.deleteAll();
        roomAssetRepository.deleteAll();
        assetRepository.deleteAll();

        AssetGroup group = createAssetGroup("Misc", BigDecimal.ZERO);

        Asset available = createAsset("Available Asset", "available", group);
        Asset inUse = createAsset("In Use Asset", "in_use", group);
        Asset deleted = createAsset("Deleted Asset", "deleted", group);

        Asset foundAvailable = assetRepository.findAvailableById(available.getId());
        assertThat(foundAvailable).isNotNull();

        assertThat(assetRepository.findAvailableById(inUse.getId())).isNull();
        assertThat(assetRepository.findAvailableById(deleted.getId())).isNull();
        assertThat(assetRepository.findAvailableById(99999L)).isNull();
    }

    @Test
    @DisplayName("findByAssetGroupId(): คืน asset ของ group ที่กำหนด")
    void findByAssetGroupId_shouldReturnAssetsOfThatGroupOnly() {

        assetEventRepository.deleteAll();
        roomAssetRepository.deleteAll();
        assetRepository.deleteAll();

        AssetGroup g1 = createAssetGroup("Group1", BigDecimal.TEN);
        AssetGroup g2 = createAssetGroup("Group2", BigDecimal.ONE);

        createAsset("G1-A1", "available", g1);
        createAsset("G1-A2", "available", g1);
        createAsset("G2-A1", "available", g2);

        List<Asset> g1Assets = assetRepository.findByAssetGroupId(g1.getId());
        assertThat(g1Assets).hasSize(2);
    }

    @Test
    @DisplayName("findAvailableAssets(): คืนเฉพาะ asset ที่ available และยังไม่ assign ห้อง")
    void findAvailableAssets_shouldReturnAvailableUnassigned() {

        assetEventRepository.deleteAll();
        roomAssetRepository.deleteAll();
        assetRepository.deleteAll();

        // ห้องของเทสนี้ ใช้เลขชุดใหม่
        Room room = createRoom(1, "AR-T5-101");
        AssetGroup group = createAssetGroup("Appliance", BigDecimal.ZERO);

        Asset free = createAsset("Free", "available", group);
        Asset assigned = createAsset("Busy", "available", group);
        Asset notAvailable = createAsset("Not Available", "in_use", group);

        linkRoomAsset(room, assigned);

        List<AssetDto> results = assetRepository.findAvailableAssets();

        assertThat(results)
                .extracting(AssetDto::getAssetName)
                .containsExactly("Free");
    }

    @Test
    @DisplayName("findInUseAssets(): คืน asset ที่ in_use และมี room")
    void findInUseAssets_shouldReturnInUseOnly() {

        assetEventRepository.deleteAll();
        roomAssetRepository.deleteAll();
        assetRepository.deleteAll();

        Room room1 = createRoom(1, "AR-T6-101");
        Room room2 = createRoom(2, "AR-T6-201");

        AssetGroup group = createAssetGroup("IT", BigDecimal.ZERO);

        Asset a1 = createAsset("PC-1", "in_use", group);
        Asset a2 = createAsset("PC-2", "in_use", group);
        createAsset("PC-3", "available", group);

        linkRoomAsset(room1, a1);
        linkRoomAsset(room2, a2);

        List<AssetDto> results = assetRepository.findInUseAssets();
        assertThat(results).hasSize(2);
    }

    @Test
    @DisplayName("findMonthlyAddonFeeByRoomId(): คืน fee ของ asset group ที่ fee > 0")
    void findMonthlyAddonFeeByRoomId_shouldReturnMonthlyFees() {

        assetEventRepository.deleteAll();
        roomAssetRepository.deleteAll();
        assetRepository.deleteAll();

        Room room = createRoom(3, "AR-T7-301");

        AssetGroup fee100 = createAssetGroup("F100", new BigDecimal("100"));
        AssetGroup fee0 = createAssetGroup("F0", BigDecimal.ZERO);

        linkRoomAsset(room, createAsset("A1", "in_use", fee100));
        linkRoomAsset(room, createAsset("A2", "in_use", fee100));
        linkRoomAsset(room, createAsset("A3", "in_use", fee0));

        List<Object[]> results = assetRepository.findMonthlyAddonFeeByRoomId(room.getId());

        assertThat(results).hasSize(2);
    }
}
