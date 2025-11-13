//package com.organicnow.backend.unit.service;
//
//import com.organicnow.backend.dto.AssetDto;
//import com.organicnow.backend.model.Asset;
//import com.organicnow.backend.model.AssetGroup;
//import com.organicnow.backend.repository.AssetGroupRepository;
//import com.organicnow.backend.repository.AssetRepository;
//import com.organicnow.backend.repository.RoomAssetRepository;
//import com.organicnow.backend.service.AssetService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.util.*;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class AssetServiceTest {
//
//    @Mock
//    private AssetRepository assetRepository;
//
//    @Mock
//    private AssetGroupRepository assetGroupRepository;
//
//    @Mock
//    private RoomAssetRepository roomAssetRepository;
//
//    @InjectMocks
//    private AssetService assetService;
//
//    private Asset asset;
//    private AssetGroup group;
//
//    @BeforeEach
//    void setup() {
//        group = new AssetGroup();
//        try {
//            group.getClass().getMethod("setName", String.class)
//                    .invoke(group, "Furniture");
//        } catch (Exception ignore) {}
//
//        asset = new Asset();
//        asset.setId(1L);
//        asset.setAssetName("Table");
//        asset.setStatus("available");
//        asset.setAssetGroup(group);
//    }
//
//    // ✅ getAllAssets()
//    @Test
//    void testGetAllAssets() {
//        AssetDto dto = new AssetDto();
//        dto.setAssetId(1L);
//        dto.setAssetName("Table");
//        dto.setAssetType("Furniture");
//        dto.setStatus("available");
//
//        when(assetRepository.findAllAssetOptions()).thenReturn(List.of(dto));
//
//        var result = assetService.getAllAssets();
//
//        assertThat(result).hasSize(1);
//        assertThat(result.get(0).getAssetName()).isEqualTo("Table");
//        verify(assetRepository, times(1)).findAllAssetOptions();
//    }
//
//    // ✅ getAssetsByRoomId()
//    @Test
//    void testGetAssetsByRoomId() {
//        AssetDto dto = new AssetDto();
//        dto.setAssetId(2L);
//        dto.setAssetName("Chair");
//        dto.setAssetType("Furniture");
//        dto.setStatus("in_use");
//
//        when(assetRepository.findAssetsByRoomId(5L)).thenReturn(List.of(dto));
//
//        var result = assetService.getAssetsByRoomId(5L);
//
//        assertThat(result).hasSize(1);
//        assertThat(result.get(0).getAssetName()).isEqualTo("Chair");
//        verify(assetRepository).findAssetsByRoomId(5L);
//    }
//
//    // ✅ createAsset() — กรณี status ว่าง → ตั้งเป็น available
//    @Test
//    void testCreateAsset_DefaultStatus() {
//        Asset input = new Asset();
//        input.setAssetName("Desk");
//
//        when(assetRepository.save(any(Asset.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//        Asset saved = assetService.createAsset(input);
//
//        assertThat(saved.getStatus()).isEqualTo("available");
//        verify(assetRepository).save(any(Asset.class));
//    }
//
//    // ✅ createAsset() — กรณีมี status อยู่แล้ว
//    @Test
//    void testCreateAsset_WithStatus() {
//        Asset input = new Asset();
//        input.setAssetName("Lamp");
//        input.setStatus("in_use");
//
//        when(assetRepository.save(any(Asset.class))).thenReturn(input);
//
//        Asset saved = assetService.createAsset(input);
//
//        assertThat(saved.getStatus()).isEqualTo("in_use");
//        verify(assetRepository).save(any(Asset.class));
//    }
//
//    // ✅ updateAsset()
//    @Test
//    void testUpdateAsset() {
//        Asset existing = new Asset();
//        existing.setId(1L);
//        existing.setAssetName("OldName");
//        existing.setStatus("available");
//
//        Asset update = new Asset();
//        update.setAssetName("NewName");
//        update.setStatus("maintenance");
//
//        when(assetRepository.findById(1L)).thenReturn(Optional.of(existing));
//        when(assetRepository.save(any(Asset.class))).thenReturn(existing);
//
//        Asset result = assetService.updateAsset(1L, update);
//
//        assertThat(result.getAssetName()).isEqualTo("NewName");
//        assertThat(result.getStatus()).isEqualTo("maintenance");
//        verify(assetRepository).save(existing);
//    }
//
//    // ✅ softDeleteAsset()
//    @Test
//    void testSoftDeleteAsset() {
//        when(assetRepository.findById(1L)).thenReturn(Optional.of(asset));
//
//        assetService.softDeleteAsset(1L);
//
//        assertThat(asset.getStatus()).isEqualTo("deleted");
//        verify(roomAssetRepository).deleteByAsset_Id(1L);
//        verify(assetRepository).save(asset);
//    }
//
//    // ✅ updateStatus()
//    @Test
//    void testUpdateStatus() {
//        when(assetRepository.findById(1L)).thenReturn(Optional.of(asset));
//        when(assetRepository.save(any(Asset.class))).thenReturn(asset);
//
//        Asset result = assetService.updateStatus(1L, "maintenance");
//
//        assertThat(result.getStatus()).isEqualTo("maintenance");
//        verify(assetRepository).save(asset);
//    }
//
//    // ✅ createBulk() — สร้างหลายชิ้นพร้อมรันชื่อ table-001, table-002
//    @Test
//    void testCreateBulk() {
//        when(assetGroupRepository.findById(2L)).thenReturn(Optional.of(group));
//        when(assetRepository.findByAssetGroupId(2L)).thenReturn(List.of());
//
//        List<Asset> toSave = new ArrayList<>();
//        when(assetRepository.saveAll(anyList())).thenAnswer(invocation -> {
//            toSave.addAll(invocation.getArgument(0));
//            return toSave;
//        });
//
//        List<Asset> result = assetService.createBulk(2L, "table", 2);
//
//        assertThat(result).hasSize(2);
//        assertThat(result.get(0).getAssetName()).isEqualTo("table-001");
//        assertThat(result.get(1).getStatus()).isEqualTo("available");
//        verify(assetRepository).saveAll(anyList());
//    }
//
//    // ❌ createBulk() — qty <= 0 → ต้อง throw
//    @Test
//    void testCreateBulk_InvalidQty() {
//        assertThatThrownBy(() -> assetService.createBulk(1L, "chair", 0))
//                .isInstanceOf(IllegalArgumentException.class)
//                .hasMessageContaining("qty must be > 0");
//    }
//
//    // ✅ getAvailableAssets()
//    @Test
//    void testGetAvailableAssets() {
//        AssetDto dto = new AssetDto();
//        dto.setAssetId(1L);
//        dto.setAssetName("Fan");
//        dto.setAssetType("Electric");
//        dto.setStatus("available");
//
//        when(assetRepository.findAvailableAssets()).thenReturn(List.of(dto));
//
//        List<AssetDto> result = assetService.getAvailableAssets();
//
//        assertThat(result).hasSize(1);
//        assertThat(result.get(0).getAssetName()).isEqualTo("Fan");
//        verify(assetRepository).findAvailableAssets();
//    }
//}
