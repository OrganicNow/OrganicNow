package com.organicnow.backend.unit.service;

import com.organicnow.backend.dto.AssetGroupDropdownDto;
import com.organicnow.backend.model.Asset;
import com.organicnow.backend.model.AssetGroup;
import com.organicnow.backend.repository.AssetGroupRepository;
import com.organicnow.backend.repository.AssetRepository;
import com.organicnow.backend.service.AssetGroupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AssetGroupServiceTest {

    private AssetGroupRepository assetGroupRepository;
    private AssetRepository assetRepository;
    private AssetGroupService service;

    @BeforeEach
    void setUp() {
        assetGroupRepository = mock(AssetGroupRepository.class);
        assetRepository = mock(AssetRepository.class);
        service = new AssetGroupService(assetGroupRepository, assetRepository);
    }

    // -------------------------------------------------------
    // ✅ TEST: getAllGroupsForDropdown()
    // -------------------------------------------------------
    @Test
    void testGetAllGroupsForDropdown_returnsMappedDtos() {

        AssetGroup g = AssetGroup.builder()
                .id(1L)
                .assetGroupName("Furniture")
                .monthlyAddonFee(BigDecimal.valueOf(200))
                .oneTimeDamageFee(BigDecimal.valueOf(500))
                .freeReplacement(true)
                .updatedAt(LocalDateTime.now())
                .build();

        when(assetGroupRepository.findAll()).thenReturn(List.of(g));

        List<AssetGroupDropdownDto> result = service.getAllGroupsForDropdown();

        assertEquals(1, result.size());
        assertEquals("Furniture", result.get(0).getName());
        assertEquals(5, result.get(0).getThreshold());
    }

    // -------------------------------------------------------
    // ✅ TEST: getAllGroups()
    // -------------------------------------------------------
    @Test
    void testGetAllGroups_returnsDtos() {

        AssetGroup g = AssetGroup.builder()
                .id(2L)
                .assetGroupName("Electronics")
                .monthlyAddonFee(BigDecimal.TEN)
                .oneTimeDamageFee(BigDecimal.ONE)
                .freeReplacement(false)
                .updatedAt(LocalDateTime.now())
                .build();

        when(assetGroupRepository.findAll()).thenReturn(List.of(g));

        List<AssetGroupDropdownDto> result = service.getAllGroups();

        assertEquals(1, result.size());
        assertEquals("Electronics", result.get(0).getName());
    }

    // -------------------------------------------------------
    // ✅ TEST: getAllAssetGroups()
    // -------------------------------------------------------
    @Test
    void testGetAllAssetGroups() {

        AssetGroup g = AssetGroup.builder().id(3L).assetGroupName("Test").build();

        when(assetGroupRepository.findAll()).thenReturn(List.of(g));

        List<AssetGroup> result = service.getAllAssetGroups();

        assertEquals(1, result.size());
        assertEquals(3L, result.get(0).getId());
    }

    // -------------------------------------------------------
    // ✅ TEST: createAssetGroup()
    // -------------------------------------------------------
    @Test
    void testCreateAssetGroup_successfullyCreated() {

        AssetGroup input = AssetGroup.builder()
                .assetGroupName("New Group")
                .monthlyAddonFee(null)   // should default -> 0
                .oneTimeDamageFee(null)  // default -> 0
                .freeReplacement(null)   // default -> true
                .build();

        when(assetGroupRepository.existsByAssetGroupName("New Group")).thenReturn(false);
        when(assetGroupRepository.save(any(AssetGroup.class))).thenAnswer(i -> i.getArgument(0));

        AssetGroup created = service.createAssetGroup(input);

        assertEquals(BigDecimal.ZERO, created.getMonthlyAddonFee());
        assertEquals(BigDecimal.ZERO, created.getOneTimeDamageFee());
        assertTrue(created.getFreeReplacement());
    }

    @Test
    void testCreateAssetGroup_duplicateName_throwsException() {

        AssetGroup input = AssetGroup.builder().assetGroupName("Dup").build();

        when(assetGroupRepository.existsByAssetGroupName("Dup")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.createAssetGroup(input));

        assertEquals("duplicate_group_name", ex.getMessage());
    }

    // -------------------------------------------------------
    // ✅ TEST: updateAssetGroup()
    // -------------------------------------------------------
    @Test
    void testUpdateAssetGroup_successfullyUpdated() {

        AssetGroup existing = AssetGroup.builder()
                .id(10L)
                .assetGroupName("OldName")
                .monthlyAddonFee(BigDecimal.ONE)
                .oneTimeDamageFee(BigDecimal.ONE)
                .freeReplacement(true)
                .build();

        AssetGroup updated = AssetGroup.builder()
                .assetGroupName("NewName")
                .monthlyAddonFee(BigDecimal.TEN)
                .oneTimeDamageFee(BigDecimal.TEN)
                .freeReplacement(false)
                .build();

        when(assetGroupRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(assetGroupRepository.existsByAssetGroupName("NewName")).thenReturn(false);
        when(assetGroupRepository.save(any(AssetGroup.class))).thenAnswer(i -> i.getArgument(0));

        AssetGroup result = service.updateAssetGroup(10L, updated);

        assertEquals("NewName", result.getAssetGroupName());
        assertEquals(BigDecimal.TEN, result.getMonthlyAddonFee());
        assertFalse(result.getFreeReplacement());
    }

    @Test
    void testUpdateAssetGroup_nameConflict_throwsException() {

        AssetGroup existing = AssetGroup.builder()
                .id(11L)
                .assetGroupName("Original")
                .build();

        AssetGroup updated = AssetGroup.builder()
                .assetGroupName("ConflictName")
                .build();

        when(assetGroupRepository.findById(11L)).thenReturn(Optional.of(existing));
        when(assetGroupRepository.existsByAssetGroupName("ConflictName")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.updateAssetGroup(11L, updated));

        assertEquals("duplicate_group_name", ex.getMessage());
    }

    @Test
    void testUpdateAssetGroup_notFound_throwsException() {

        when(assetGroupRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.updateAssetGroup(99L, new AssetGroup()));

        assertTrue(ex.getMessage().contains("Asset Group not found with id 99"));
    }

    // -------------------------------------------------------
    // ✅ TEST: deleteAssetGroup()
    // -------------------------------------------------------
    @Test
    void testDeleteAssetGroup_deletesAssetsAndGroup() {

        Asset a1 = Asset.builder().id(1L).build();
        Asset a2 = Asset.builder().id(2L).build();

        when(assetRepository.findByAssetGroupId(55L))
                .thenReturn(List.of(a1, a2));

        int deletedCount = service.deleteAssetGroup(55L);

        assertEquals(2, deletedCount);
        verify(assetRepository, times(1)).deleteAll(List.of(a1, a2));
        verify(assetGroupRepository, times(1)).deleteById(55L);
    }

    @Test
    void testDeleteAssetGroup_noAssets_stillDeletesGroup() {

        when(assetRepository.findByAssetGroupId(100L))
                .thenReturn(List.of());

        int deletedCount = service.deleteAssetGroup(100L);

        assertEquals(0, deletedCount);
        verify(assetRepository, never()).deleteAll(any());
        verify(assetGroupRepository, times(1)).deleteById(100L);
    }
}
