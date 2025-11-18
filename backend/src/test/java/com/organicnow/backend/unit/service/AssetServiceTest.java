package com.organicnow.backend.unit.service;

import com.organicnow.backend.dto.AssetDto;
import com.organicnow.backend.model.Asset;
import com.organicnow.backend.model.AssetGroup;
import com.organicnow.backend.repository.AssetGroupRepository;
import com.organicnow.backend.repository.AssetRepository;
import com.organicnow.backend.repository.RoomAssetRepository;
import com.organicnow.backend.service.AssetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AssetServiceTest {

    private AssetRepository assetRepository;
    private AssetGroupRepository assetGroupRepository;
    private RoomAssetRepository roomAssetRepository;
    private AssetService service;

    @BeforeEach
    void setup() {
        assetRepository = mock(AssetRepository.class);
        assetGroupRepository = mock(AssetGroupRepository.class);
        roomAssetRepository = mock(RoomAssetRepository.class);
        service = new AssetService(assetRepository, assetGroupRepository, roomAssetRepository);
    }

    // -------------------------------------------------------
    // ✅ TEST: getAllAssets()
    // -------------------------------------------------------
    @Test
    void testGetAllAssets_returnsList() {
        AssetDto dto = new AssetDto();
        when(assetRepository.findAllAssetOptions()).thenReturn(List.of(dto));

        List<AssetDto> result = service.getAllAssets();
        assertEquals(1, result.size());
    }

    // -------------------------------------------------------
    // ✅ TEST: getAssetsByRoomId()
    // -------------------------------------------------------
    @Test
    void testGetAssetsByRoomId_returnsList() {
        AssetDto dto = new AssetDto();
        when(assetRepository.findAssetsByRoomId(5L)).thenReturn(List.of(dto));

        List<AssetDto> result = service.getAssetsByRoomId(5L);
        assertEquals(1, result.size());
    }

    // -------------------------------------------------------
    // ✅ TEST: getAvailableAssets()
    // -------------------------------------------------------
    @Test
    void testGetAvailableAssets() {
        when(assetRepository.findAvailableAssets()).thenReturn(List.of(new AssetDto()));

        assertEquals(1, service.getAvailableAssets().size());
    }

    // -------------------------------------------------------
    // 🆕 getInUseAssets()
    // -------------------------------------------------------
    @Test
    void testGetInUseAssets() {
        when(assetRepository.findInUseAssets()).thenReturn(List.of(new AssetDto()));

        assertEquals(1, service.getInUseAssets().size());
    }

    // -------------------------------------------------------
    // ✅ TEST: createAsset()
    // -------------------------------------------------------
    @Test
    void testCreateAsset_setsDefaultStatus() {

        Asset input = Asset.builder()
                .assetName("Chair")
                .status(null)    // should default to available
                .build();

        when(assetRepository.save(any(Asset.class))).thenAnswer(i -> i.getArgument(0));

        Asset result = service.createAsset(input);

        assertEquals("available", result.getStatus());
    }

    @Test
    void testCreateAsset_savesAsIsIfStatusProvided() {

        Asset input = Asset.builder()
                .assetName("Table")
                .status("in_use")
                .build();

        when(assetRepository.save(any(Asset.class))).thenAnswer(i -> i.getArgument(0));

        Asset result = service.createAsset(input);
        assertEquals("in_use", result.getStatus());
    }

    // -------------------------------------------------------
    // ✅ TEST: updateAsset()
    // -------------------------------------------------------
    @Test
    void testUpdateAsset_success() {

        Asset existing = Asset.builder()
                .id(10L)
                .assetName("Old")
                .status("available")
                .assetGroup(new AssetGroup())
                .build();

        Asset updated = Asset.builder()
                .assetName("New Name")
                .assetGroup(new AssetGroup())
                .status("in_use")
                .build();

        when(assetRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(assetRepository.save(any(Asset.class))).thenAnswer(i -> i.getArgument(0));

        Asset result = service.updateAsset(10L, updated);

        assertEquals("New Name", result.getAssetName());
        assertEquals("in_use", result.getStatus());
    }

    @Test
    void testUpdateAsset_ignoresBlankStatus() {

        Asset existing = Asset.builder()
                .id(10L)
                .assetName("Old")
                .status("available")
                .assetGroup(new AssetGroup())
                .build();

        Asset updated = Asset.builder()
                .assetName("Updated")
                .status("")  // blank -> must ignore
                .build();

        when(assetRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(assetRepository.save(any(Asset.class))).thenAnswer(i -> i.getArgument(0));

        Asset result = service.updateAsset(10L, updated);

        assertEquals("available", result.getStatus()); // unchanged
    }

    // -------------------------------------------------------
    // ✅ TEST: softDeleteAsset()
    // -------------------------------------------------------
    @Test
    void testSoftDeleteAsset_marksDeletedAndRemovesRoomRelation() {

        Asset existing = Asset.builder()
                .id(99L)
                .assetName("Chair")
                .status("available")
                .build();

        when(assetRepository.findById(99L)).thenReturn(Optional.of(existing));
        when(assetRepository.save(any(Asset.class))).thenAnswer(i -> i.getArgument(0));

        service.softDeleteAsset(99L);

        verify(roomAssetRepository, times(1)).deleteByAsset_Id(99L);
        assertEquals("deleted", existing.getStatus());
    }

    // -------------------------------------------------------
    // ✅ TEST: updateStatus()
    // -------------------------------------------------------
    @Test
    void testUpdateStatus_updatesStatus() {

        Asset existing = Asset.builder()
                .id(77L)
                .status("available")
                .build();

        when(assetRepository.findById(77L)).thenReturn(Optional.of(existing));
        when(assetRepository.save(any(Asset.class))).thenAnswer(i -> i.getArgument(0));

        Asset result = service.updateStatus(77L, "in_use");

        assertEquals("in_use", result.getStatus());
    }

    // -------------------------------------------------------
    // 🆕 TEST: markAssetInUse()
    // -------------------------------------------------------
    @Test
    void testMarkAssetInUse() {

        Asset existing = Asset.builder().id(5L).status("available").build();

        when(assetRepository.findById(5L)).thenReturn(Optional.of(existing));

        service.markAssetInUse(5L);

        assertEquals("in_use", existing.getStatus());
        verify(assetRepository).save(existing);
    }

    // -------------------------------------------------------
    // 🆕 TEST: markAssetAvailable()
    // -------------------------------------------------------
    @Test
    void testMarkAssetAvailable() {

        Asset existing = Asset.builder().id(5L).status("in_use").build();

        when(assetRepository.findById(5L)).thenReturn(Optional.of(existing));

        service.markAssetAvailable(5L);

        assertEquals("available", existing.getStatus());
        verify(assetRepository).save(existing);
    }

    // -------------------------------------------------------
    // 🆕 TEST: createBulk()
    // -------------------------------------------------------
    @Test
    void testCreateBulk_successfullyCreatesAssets() {

        AssetGroup group = AssetGroup.builder()
                .id(3L)
                .assetGroupName("Chair Group")
                .build();

        when(assetGroupRepository.findById(3L)).thenReturn(Optional.of(group));

        // Existing assets → for calculating index
        Asset a1 = Asset.builder().assetName("Chair-001").build();
        Asset a2 = Asset.builder().assetName("Chair-002").build();
        when(assetRepository.findByAssetGroupId(3L)).thenReturn(List.of(a1, a2));

        ArgumentCaptor<List<Asset>> captor = ArgumentCaptor.forClass(List.class);
        when(assetRepository.saveAll(any())).thenAnswer(i -> i.getArgument(0));

        List<Asset> result = service.createBulk(3L, "Chair", 3);

        assertEquals(3, result.size());
        assertEquals("Chair-003", result.get(0).getAssetName());
        assertEquals("Chair-004", result.get(1).getAssetName());
        assertEquals("Chair-005", result.get(2).getAssetName());
    }

    @Test
    void testCreateBulk_qtyMustBePositive() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.createBulk(1L, "Chair", 0)
        );
        assertEquals("qty must be > 0", ex.getMessage());
    }

    @Test
    void testCreateBulk_groupNotFound() {
        when(assetGroupRepository.findById(10L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.createBulk(10L, "Chair", 2)
        );

        assertEquals("AssetGroup not found", ex.getMessage());
    }
}
