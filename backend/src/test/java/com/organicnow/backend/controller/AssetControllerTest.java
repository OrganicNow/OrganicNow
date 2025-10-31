//package com.organicnow.backend.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.organicnow.backend.dto.AssetDto;
//import com.organicnow.backend.model.Asset;
//import com.organicnow.backend.service.AssetService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mockito;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.util.List;
//
//import static org.mockito.ArgumentMatchers.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@WebMvcTest(AssetController.class)
//class AssetControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockBean
//    private AssetService assetService;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    private Asset asset;
//    private AssetDto assetDto;
//
//    @BeforeEach
//    void setup() {
//        asset = new Asset();
//        asset.setId(1L);
//        asset.setAssetName("Table");
//
//        assetDto = new AssetDto();
//        assetDto.setAssetId(1L);
//        assetDto.setAssetName("Table");
//    }
//
//    // ✅ Test: GET /assets/{roomId}
//    @Test
//    void testGetAssetsByRoomId() throws Exception {
//        Mockito.when(assetService.getAssetsByRoomId(1L)).thenReturn(List.of(assetDto));
//
//        mockMvc.perform(get("/assets/1"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value("success"))
//                .andExpect(jsonPath("$.result[0].assetName").value("Table"));
//    }
//
//    // ✅ Test: GET /assets/all
//    @Test
//    void testGetAllAssets() throws Exception {
//        Mockito.when(assetService.getAllAssets()).thenReturn(List.of(assetDto));
//
//        mockMvc.perform(get("/assets/all"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value("success"))
//                .andExpect(jsonPath("$.result[0].assetName").value("Table"));
//    }
//
//    // ✅ Test: GET /assets/available
//    @Test
//    void testGetAvailableAssets() throws Exception {
//        Mockito.when(assetService.getAvailableAssets()).thenReturn(List.of(assetDto));
//
//        mockMvc.perform(get("/assets/available"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value("success"))
//                .andExpect(jsonPath("$.result[0].assetName").value("Table"));
//    }
//
//    // ✅ Test: POST /assets/bulk
//    @Test
//    void testCreateBulk() throws Exception {
//        Mockito.when(assetService.createBulk(2L, "table", 3))
//                .thenReturn(List.of(new Asset(), new Asset(), new Asset()));
//
//        mockMvc.perform(post("/assets/bulk")
//                        .param("assetGroupId", "2")
//                        .param("name", "table")
//                        .param("qty", "3"))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.status").value("success"))
//                .andExpect(jsonPath("$.result").value(3));
//    }
//
//    // ✅ Test: POST /assets/create
//    @Test
//    void testCreateAsset() throws Exception {
//        Mockito.when(assetService.createAsset(any(Asset.class))).thenReturn(asset);
//
//        mockMvc.perform(post("/assets/create")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(asset)))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.assetName").value("Table"));
//    }
//
//    // ✅ Test: PUT /assets/update/{id}
//    @Test
//    void testUpdateAsset() throws Exception {
//        Mockito.when(assetService.updateAsset(eq(1L), any(Asset.class))).thenReturn(asset);
//
//        mockMvc.perform(put("/assets/update/1")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(asset)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.assetName").value("Table"));
//    }
//
//    // ✅ Test: DELETE /assets/{id}
//    @Test
//    void testSoftDeleteAsset() throws Exception {
//        mockMvc.perform(delete("/assets/1"))
//                .andExpect(status().isNoContent());
//
//        Mockito.verify(assetService, Mockito.times(1)).softDeleteAsset(1L);
//    }
//
//    // ✅ Test: PATCH /assets/{id}/status
//    @Test
//    void testUpdateStatus() throws Exception {
//        asset.setStatus("maintenance");
//
//        Mockito.when(assetService.updateStatus(eq(1L), eq("maintenance")))
//                .thenReturn(asset);
//
//        AssetController.UpdateStatusReq req = new AssetController.UpdateStatusReq();
//        req.setStatus("maintenance");
//
//        mockMvc.perform(patch("/assets/1/status")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(req)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value("maintenance"));
//    }
//}
