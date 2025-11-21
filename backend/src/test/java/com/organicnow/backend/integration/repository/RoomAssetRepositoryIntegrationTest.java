package com.organicnow.backend.integration.repository;

import com.organicnow.backend.dto.AssetDto;
import com.organicnow.backend.model.Asset;
import com.organicnow.backend.model.AssetGroup;
import com.organicnow.backend.model.Room;
import com.organicnow.backend.model.RoomAsset;
import com.organicnow.backend.repository.RoomAssetRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Transactional
@DisplayName("Integration Test: RoomAssetRepository")
class RoomAssetRepositoryIntegrationTest {

    @Autowired
    private RoomAssetRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    private Room room1, room2;
    private AssetGroup assetGroup;
    private Asset asset1, asset2, asset3;

    @BeforeEach
    void setUp() {
        // Clear all data with CASCADE to handle foreign keys
        entityManager.getEntityManager()
                .createNativeQuery("TRUNCATE TABLE maintenance_notification_skip, maintenance_schedule, " +
                        "asset_event, room_asset, asset, asset_group, room, tenant, " +
                        "contract, contract_file, invoice, invoice_item, fee, maintain, " +
                        "payment_proofs, payment_records, package_plan, contract_type, admin " +
                        "RESTART IDENTITY CASCADE")
                .executeUpdate();
        entityManager.flush();
        entityManager.clear();

        // Create test data
        setupTestData();
    }

    private void setupTestData() {
        // Create rooms
        room1 = new Room();
        room1.setRoomNumber("101");
        room1.setRoomFloor(1);
        room1.setRoomSize(25);
        entityManager.persistAndFlush(room1);

        room2 = new Room();
        room2.setRoomNumber("102");
        room2.setRoomFloor(1);
        room2.setRoomSize(30);
        entityManager.persistAndFlush(room2);

        // Create asset group
        assetGroup = new AssetGroup();
        assetGroup.setAssetGroupName("Electronic Appliances");
        entityManager.persistAndFlush(assetGroup);

        // Create assets
        asset1 = new Asset();
        asset1.setAssetName("Air Conditioner");
        asset1.setAssetGroup(assetGroup);
        asset1.setStatus("ACTIVE");
        entityManager.persistAndFlush(asset1);

        asset2 = new Asset();
        asset2.setAssetName("Refrigerator");
        asset2.setAssetGroup(assetGroup);
        asset2.setStatus("ACTIVE");
        entityManager.persistAndFlush(asset2);

        asset3 = new Asset();
        asset3.setAssetName("Microwave");
        asset3.setAssetGroup(assetGroup);
        asset3.setStatus("ACTIVE");
        entityManager.persistAndFlush(asset3);

        // Create room assets
        RoomAsset roomAsset1 = new RoomAsset();
        roomAsset1.setRoom(room1);
        roomAsset1.setAsset(asset1);
        entityManager.persistAndFlush(roomAsset1);

        RoomAsset roomAsset2 = new RoomAsset();
        roomAsset2.setRoom(room1);
        roomAsset2.setAsset(asset2);
        entityManager.persistAndFlush(roomAsset2);

        RoomAsset roomAsset3 = new RoomAsset();
        roomAsset3.setRoom(room2);
        roomAsset3.setAsset(asset3);
        entityManager.persistAndFlush(roomAsset3);
    }

    @Test
    @DisplayName("findAssetsByRoomId: should return all assets for a specific room")
    void findAssetsByRoomId_ShouldReturnAssetsForRoom() {
        // When
        List<AssetDto> result = repository.findAssetsByRoomId(room1.getId());

        // Then
        assertThat(result)
                .hasSize(2)
                .extracting(AssetDto::getAssetName)
                .containsExactlyInAnyOrder("Air Conditioner", "Refrigerator");

        assertThat(result)
                .allMatch(dto -> dto.getAssetGroupName().equals("Electronic Appliances"))
                .allMatch(dto -> dto.getFloor() == 1)
                .allMatch(dto -> dto.getRoom().equals("101"));
    }

    @Test
    @DisplayName("findAssetsByRoomId: should return empty list for room with no assets")
    void findAssetsByRoomId_ShouldReturnEmptyListForRoomWithoutAssets() {
        // Given: Create a new room without assets
        Room emptyRoom = new Room();
        emptyRoom.setRoomNumber("103");
        emptyRoom.setRoomFloor(1);
        emptyRoom.setRoomSize(25);
        entityManager.persistAndFlush(emptyRoom);

        // When
        List<AssetDto> result = repository.findAssetsByRoomId(emptyRoom.getId());

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("existsByRoomIdAndAssetId: should return true when room asset exists")
    void existsByRoomIdAndAssetId_ShouldReturnTrueWhenExists() {
        // When
        boolean exists = repository.existsByRoomIdAndAssetId(room1.getId(), asset1.getId());

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByRoomIdAndAssetId: should return false when room asset does not exist")
    void existsByRoomIdAndAssetId_ShouldReturnFalseWhenNotExists() {
        // When
        boolean exists = repository.existsByRoomIdAndAssetId(room1.getId(), asset3.getId());

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("findByRoomIdAndAssetId: should return room asset when it exists")
    void findByRoomIdAndAssetId_ShouldReturnRoomAssetWhenExists() {
        // When
        Optional<RoomAsset> result = repository.findByRoomIdAndAssetId(room1.getId(), asset1.getId());

        // Then
        assertThat(result)
                .isPresent()
                .get()
                .satisfies(ra -> {
                    assertThat(ra.getRoom().getId()).isEqualTo(room1.getId());
                    assertThat(ra.getAsset().getId()).isEqualTo(asset1.getId());
                });
    }

    @Test
    @DisplayName("findByRoomIdAndAssetId: should return empty optional when not exists")
    void findByRoomIdAndAssetId_ShouldReturnEmptyWhenNotExists() {
        // When
        Optional<RoomAsset> result = repository.findByRoomIdAndAssetId(room1.getId(), asset3.getId());

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByRoomId: should return all room assets for a specific room")
    void findByRoomId_ShouldReturnAllRoomAssetsForRoom() {
        // When
        List<RoomAsset> result = repository.findByRoomId(room1.getId());

        // Then
        assertThat(result)
                .hasSize(2)
                .extracting(ra -> ra.getAsset().getId())
                .containsExactlyInAnyOrder(asset1.getId(), asset2.getId());
    }

    @Test
    @DisplayName("findByRoomId: should return empty list for room without assets")
    void findByRoomId_ShouldReturnEmptyListForRoomWithoutAssets() {
        // Given: Create a new room without assets
        Room emptyRoom = new Room();
        emptyRoom.setRoomNumber("104");
        emptyRoom.setRoomFloor(2);
        emptyRoom.setRoomSize(25);
        entityManager.persistAndFlush(emptyRoom);

        // When
        List<RoomAsset> result = repository.findByRoomId(emptyRoom.getId());

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("deleteByAsset_Id: should delete all room assets for a specific asset")
    void deleteByAsset_Id_ShouldDeleteAllRoomAssetsForAsset() {
        // Given
        assertThat(repository.findByRoomId(room1.getId())).hasSize(2);

        // When: Delete asset1 from all rooms
        repository.deleteByAsset_Id(asset1.getId());
        entityManager.flush();

        // Then: room1 should only have asset2 left
        assertThat(repository.findByRoomId(room1.getId()))
                .hasSize(1)
                .extracting(ra -> ra.getAsset().getId())
                .containsExactly(asset2.getId());

        // And asset1 should not exist in any room
        assertThat(repository.existsByRoomIdAndAssetId(room1.getId(), asset1.getId())).isFalse();
    }

    @Test
    @DisplayName("findAssetsByRoomIds: should return assets for multiple rooms")
    void findAssetsByRoomIds_ShouldReturnAssetsForMultipleRooms() {
        // When
        List<Object[]> result = repository.findAssetsByRoomIds(Arrays.asList(room1.getId(), room2.getId()));

        // Then
        assertThat(result).hasSize(3);

        // Verify room1 assets
        List<Object[]> room1Results = result.stream()
                .filter(row -> ((Long) row[0]).equals(room1.getId()))
                .toList();
        assertThat(room1Results).hasSize(2);

        // Verify room2 assets
        List<Object[]> room2Results = result.stream()
                .filter(row -> ((Long) row[0]).equals(room2.getId()))
                .toList();
        assertThat(room2Results).hasSize(1);

        // Verify data structure
        Object[] firstRow = result.get(0);
        assertThat(firstRow).hasSize(6);
        assertThat(firstRow[0]).isInstanceOf(Long.class); // roomId
        assertThat(firstRow[1]).isInstanceOf(Long.class); // assetId
        assertThat(firstRow[2]).isInstanceOf(String.class); // assetName
        assertThat(firstRow[3]).isInstanceOf(String.class); // assetGroupName
        assertThat(firstRow[4]).isInstanceOf(Integer.class); // roomFloor
        assertThat(firstRow[5]).isInstanceOf(String.class); // roomNumber
    }

    @Test
    @DisplayName("findAssetsByRoomIds: should return empty list when no matching rooms")
    void findAssetsByRoomIds_ShouldReturnEmptyListForNonExistentRooms() {
        // When
        List<Object[]> result = repository.findAssetsByRoomIds(Arrays.asList(999L, 1000L));

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("existsByRoomId: should return true when room has assets")
    void existsByRoomId_ShouldReturnTrueWhenRoomHasAssets() {
        // When
        boolean exists = repository.existsByRoomId(room1.getId());

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByRoomId: should return false when room has no assets")
    void existsByRoomId_ShouldReturnFalseWhenRoomHasNoAssets() {
        // Given: Create a new room without assets
        Room emptyRoom = new Room();
        emptyRoom.setRoomNumber("105");
        emptyRoom.setRoomFloor(2);
        emptyRoom.setRoomSize(25);
        entityManager.persistAndFlush(emptyRoom);

        // When
        boolean exists = repository.existsByRoomId(emptyRoom.getId());

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("save: should successfully create a new room asset")
    void save_ShouldSuccessfullyCreateNewRoomAsset() {
        // Given: Create a new room and asset
        Room newRoom = new Room();
        newRoom.setRoomNumber("106");
        newRoom.setRoomFloor(2);
        newRoom.setRoomSize(25);
        entityManager.persistAndFlush(newRoom);

        Asset newAsset = new Asset();
        newAsset.setAssetName("Television");
        newAsset.setAssetGroup(assetGroup);
        newAsset.setStatus("ACTIVE");
        entityManager.persistAndFlush(newAsset);

        RoomAsset roomAsset = new RoomAsset();
        roomAsset.setRoom(newRoom);
        roomAsset.setAsset(newAsset);

        // When
        RoomAsset saved = repository.save(roomAsset);
        entityManager.flush();

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(repository.existsByRoomIdAndAssetId(newRoom.getId(), newAsset.getId())).isTrue();
    }

    @Test
    @DisplayName("delete: should successfully delete a room asset")
    void delete_ShouldSuccessfullyDeleteRoomAsset() {
        // Given: Get an existing room asset
        RoomAsset roomAsset = repository.findByRoomIdAndAssetId(room1.getId(), asset1.getId()).orElseThrow();

        // When
        repository.delete(roomAsset);
        entityManager.flush();

        // Then
        assertThat(repository.existsByRoomIdAndAssetId(room1.getId(), asset1.getId())).isFalse();
    }

}