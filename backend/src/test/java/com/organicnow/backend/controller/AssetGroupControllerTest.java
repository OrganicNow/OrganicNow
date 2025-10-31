//package com.organicnow.backend.controller;
//
//import com.organicnow.backend.model.AssetGroup;
//import com.organicnow.backend.service.AssetGroupService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.ArgumentCaptor;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.http.ResponseEntity;
//
//import java.util.List;
//import java.util.Map;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//class AssetGroupControllerTest {
//
//    @Mock
//    private AssetGroupService assetGroupService;
//
//    @InjectMocks
//    private AssetGroupController assetGroupController;
//
//    private AssetGroup sampleGroup;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//
//        // ✅ สร้าง mock data
//        sampleGroup = new AssetGroup();
//        try {
//            sampleGroup.getClass().getMethod("setId", Long.class).invoke(sampleGroup, 1L);
//        } catch (Exception ignored) {}
//        try {
//            sampleGroup.getClass().getMethod("setGroupName", String.class).invoke(sampleGroup, "Electrical");
//        } catch (Exception ignored) {}
//    }
//
//    // ✅ ทดสอบ getAllAssetGroups()
//    @Test
//    void testGetAllAssetGroups() {
//        when(assetGroupService.getAllAssetGroups()).thenReturn(List.of(sampleGroup));
//
//        ResponseEntity<List<AssetGroup>> response = assetGroupController.getAllAssetGroups();
//
//        assertEquals(200, response.getStatusCode().value());
//        assertNotNull(response.getBody());
//        assertEquals(1, response.getBody().size());
//        verify(assetGroupService, times(1)).getAllAssetGroups();
//    }
//
//    // ✅ ทดสอบ createAssetGroup()
//    @Test
//    void testCreateAssetGroup() {
//        when(assetGroupService.createAssetGroup(any(AssetGroup.class))).thenReturn(sampleGroup);
//
//        ResponseEntity<AssetGroup> response = assetGroupController.createAssetGroup(sampleGroup);
//
//        assertEquals(201, response.getStatusCode().value());
//        assertNotNull(response.getBody());
//        verify(assetGroupService, times(1)).createAssetGroup(sampleGroup);
//    }
//
//    // ✅ ทดสอบ updateAssetGroup()
//    @Test
//    void testUpdateAssetGroup() {
//        when(assetGroupService.updateAssetGroup(eq(1L), any(AssetGroup.class))).thenReturn(sampleGroup);
//
//        ResponseEntity<AssetGroup> response = assetGroupController.updateAssetGroup(1L, sampleGroup);
//
//        assertEquals(200, response.getStatusCode().value());
//        assertNotNull(response.getBody());
//        verify(assetGroupService, times(1)).updateAssetGroup(1L, sampleGroup);
//    }
//
//    // ✅ ทดสอบ deleteAssetGroup()
//    @Test
//    void testDeleteAssetGroup() {
//        when(assetGroupService.deleteAssetGroup(1L)).thenReturn(3); // mock ว่าลบ asset ไป 3 ตัว
//
//        ResponseEntity<Map<String, Object>> response = assetGroupController.deleteAssetGroup(1L);
//
//        assertEquals(200, response.getStatusCode().value());
//        assertNotNull(response.getBody());
//        assertEquals("deleted_group", response.getBody().get("message"));
//        assertEquals(3, response.getBody().get("deletedAssets"));
//        verify(assetGroupService, times(1)).deleteAssetGroup(1L);
//    }
//
//    // ✅ ตรวจสอบ argument ที่ส่งเข้า service ตอน create
//    @Test
//    void testCreateAssetGroupArgument() {
//        when(assetGroupService.createAssetGroup(any(AssetGroup.class))).thenReturn(sampleGroup);
//
//        assetGroupController.createAssetGroup(sampleGroup);
//
//        ArgumentCaptor<AssetGroup> captor = ArgumentCaptor.forClass(AssetGroup.class);
//        verify(assetGroupService).createAssetGroup(captor.capture());
//
//        assertNotNull(captor.getValue());
//    }
//}
