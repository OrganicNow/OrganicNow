//package com.organicnow.backend.service;
//
//import com.organicnow.backend.dto.AssetGroupDropdownDto;
//import com.organicnow.backend.model.Asset;
//import com.organicnow.backend.model.AssetGroup;
//import com.organicnow.backend.repository.AssetGroupRepository;
//import com.organicnow.backend.repository.AssetRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//class AssetGroupServiceTest {
//
//    @Mock
//    private AssetGroupRepository assetGroupRepository;
//
//    @Mock
//    private AssetRepository assetRepository;
//
//    @InjectMocks
//    private AssetGroupService assetGroupService;
//
//    private AssetGroup sampleGroup;
//    private Asset sampleAsset;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//
//        sampleGroup = new AssetGroup();
//        try {
//            sampleGroup.getClass().getMethod("setId", Long.class).invoke(sampleGroup, 1L);
//            sampleGroup.getClass().getMethod("setAssetGroupName", String.class).invoke(sampleGroup, "Electrical");
//        } catch (Exception ignored) {}
//
//        sampleAsset = new Asset();
//        try {
//            sampleAsset.getClass().getMethod("setId", Long.class).invoke(sampleAsset, 10L);
//            sampleAsset.getClass().getMethod("setAssetName", String.class).invoke(sampleAsset, "Air Conditioner");
//        } catch (Exception ignored) {}
//    }
//
//    // ✅ getAllGroupsForDropdown()
//    @Test
//    void testGetAllGroupsForDropdown() {
//        when(assetGroupRepository.findAll()).thenReturn(List.of(sampleGroup));
//
//        List<AssetGroupDropdownDto> result = assetGroupService.getAllGroupsForDropdown();
//
//        assertEquals(1, result.size());
//        assertEquals("Electrical", result.get(0).getName());
//        verify(assetGroupRepository, times(1)).findAll();
//    }
//
//    // ✅ getAllAssetGroups()
//    @Test
//    void testGetAllAssetGroups() {
//        when(assetGroupRepository.findAll()).thenReturn(List.of(sampleGroup));
//
//        List<AssetGroup> result = assetGroupService.getAllAssetGroups();
//
//        assertEquals(1, result.size());
//        assertEquals(sampleGroup, result.get(0));
//        verify(assetGroupRepository, times(1)).findAll();
//    }
//
//    // ✅ createAssetGroup() - success
//    @Test
//    void testCreateAssetGroup_Success() {
//        when(assetGroupRepository.existsByAssetGroupName("Electrical")).thenReturn(false);
//        when(assetGroupRepository.save(sampleGroup)).thenReturn(sampleGroup);
//
//        AssetGroup result = assetGroupService.createAssetGroup(sampleGroup);
//
//        assertNotNull(result);
//        assertEquals(sampleGroup, result);
//        verify(assetGroupRepository).save(sampleGroup);
//    }
//
//    // ✅ createAssetGroup() - duplicate name
//    @Test
//    void testCreateAssetGroup_DuplicateName() {
//        when(assetGroupRepository.existsByAssetGroupName("Electrical")).thenReturn(true);
//
//        RuntimeException exception = assertThrows(RuntimeException.class,
//                () -> assetGroupService.createAssetGroup(sampleGroup));
//
//        assertEquals("duplicate_group_name", exception.getMessage());
//        verify(assetGroupRepository, never()).save(any());
//    }
//
//    // ✅ updateAssetGroup() - success
//    @Test
//    void testUpdateAssetGroup_Success() {
//        AssetGroup updatedGroup = new AssetGroup();
//        try {
//            updatedGroup.getClass().getMethod("setAssetGroupName", String.class).invoke(updatedGroup, "Mechanical");
//        } catch (Exception ignored) {}
//
//        when(assetGroupRepository.findById(1L)).thenReturn(Optional.of(sampleGroup));
//        when(assetGroupRepository.existsByAssetGroupName("Mechanical")).thenReturn(false);
//        when(assetGroupRepository.save(any())).thenReturn(updatedGroup);
//
//        AssetGroup result = assetGroupService.updateAssetGroup(1L, updatedGroup);
//
//        assertEquals("Mechanical", result.getAssetGroupName());
//        verify(assetGroupRepository).save(any());
//    }
//
//    // ✅ updateAssetGroup() - not found
//    @Test
//    void testUpdateAssetGroup_NotFound() {
//        when(assetGroupRepository.findById(99L)).thenReturn(Optional.empty());
//
//        RuntimeException exception = assertThrows(RuntimeException.class,
//                () -> assetGroupService.updateAssetGroup(99L, sampleGroup));
//
//        assertTrue(exception.getMessage().contains("Asset Group not found"));
//        verify(assetGroupRepository, never()).save(any());
//    }
//
//    // ✅ updateAssetGroup() - duplicate name
//    @Test
//    void testUpdateAssetGroup_DuplicateName() {
//        AssetGroup updatedGroup = new AssetGroup();
//        try {
//            updatedGroup.getClass().getMethod("setAssetGroupName", String.class).invoke(updatedGroup, "Mechanical");
//        } catch (Exception ignored) {}
//
//        when(assetGroupRepository.findById(1L)).thenReturn(Optional.of(sampleGroup));
//        when(assetGroupRepository.existsByAssetGroupName("Mechanical")).thenReturn(true);
//
//        RuntimeException exception = assertThrows(RuntimeException.class,
//                () -> assetGroupService.updateAssetGroup(1L, updatedGroup));
//
//        assertEquals("duplicate_group_name", exception.getMessage());
//    }
//
//    // ✅ deleteAssetGroup() - with assets
//    @Test
//    void testDeleteAssetGroup_WithAssets() {
//        when(assetRepository.findByAssetGroupId(1L)).thenReturn(List.of(sampleAsset));
//
//        int deletedCount = assetGroupService.deleteAssetGroup(1L);
//
//        assertEquals(1, deletedCount);
//        verify(assetRepository, times(1)).deleteAll(any());
//        verify(assetGroupRepository, times(1)).deleteById(1L);
//    }
//
//    // ✅ deleteAssetGroup() - no assets
//    @Test
//    void testDeleteAssetGroup_NoAssets() {
//        when(assetRepository.findByAssetGroupId(1L)).thenReturn(List.of());
//
//        int deletedCount = assetGroupService.deleteAssetGroup(1L);
//
//        assertEquals(0, deletedCount);
//        verify(assetRepository, never()).deleteAll(any());
//        verify(assetGroupRepository, times(1)).deleteById(1L);
//    }
//}
