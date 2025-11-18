package com.organicnow.backend.integration.controller;

import com.organicnow.backend.controller.TenantController;
import com.organicnow.backend.dto.CreateTenantContractRequest;
import com.organicnow.backend.dto.TenantDetailDto;
import com.organicnow.backend.dto.TenantDto;
import com.organicnow.backend.dto.UpdateTenantContractRequest;
import com.organicnow.backend.service.ContractFileService;
import com.organicnow.backend.service.TenantContractService;
import com.organicnow.backend.service.TenantService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;


import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TenantController.class)
@AutoConfigureMockMvc(addFilters = false)
class TenantControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    TenantService tenantService;

    @MockBean
    TenantContractService tenantContractService;

    @MockBean
    ContractFileService contractFileService;

    // ---------- /tenant/list ----------

    @Test
    void list_whenServiceReturnsList_shouldSetHasSignedPdfAndReturnList() throws Exception {

        TenantDto t1 = new TenantDto();
        t1.setContractId(1L);

        TenantDto t2 = new TenantDto();
        t2.setContractId(2L);

        // ❌ ของเดิม (ผิด type)
        // Mockito.when(tenantService.list())
        //        .thenReturn(Arrays.asList(t1, t2));

        // ✅ แก้ให้ตรงกับ method จริง: list() คืน Map<String, Object>
        Map<String, Object> resp = new HashMap<>();
        resp.put("results", Arrays.asList(t1, t2));

        Mockito.when(tenantService.list()).thenReturn(resp);

        Mockito.when(contractFileService.hasSignedFile(1L)).thenReturn(true);
        Mockito.when(contractFileService.hasSignedFile(2L)).thenReturn(false);

        mockMvc.perform(get("/tenant/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].contractId").value(1))
                .andExpect(jsonPath("$.results[0].hasSignedPdf").value(true))
                .andExpect(jsonPath("$.results[1].contractId").value(2))
                .andExpect(jsonPath("$.results[1].hasSignedPdf").value(false));

        Mockito.verify(tenantService).list();
        Mockito.verify(contractFileService).hasSignedFile(1L);
        Mockito.verify(contractFileService).hasSignedFile(2L);
    }



    @Test
    @DisplayName("GET /tenant/list เมื่อ service ส่ง Map {results: [...]} ควรใส่ hasSignedPdf ใน results แล้วคืน Map")
    void list_whenServiceReturnsMap_shouldSetHasSignedPdfInsideResults() throws Exception {
        TenantDto t1 = new TenantDto();
        t1.setContractId(10L);
        TenantDto t2 = new TenantDto();
        t2.setContractId(20L);

        Map<String, Object> resp = new HashMap<>();
        resp.put("results", List.of(t1, t2));
        resp.put("total", 2);

        Mockito.when(tenantService.list()).thenReturn(resp);
        Mockito.when(contractFileService.hasSignedFile(10L)).thenReturn(true);
        Mockito.when(contractFileService.hasSignedFile(20L)).thenReturn(true);

        mockMvc.perform(get("/tenant/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.results[0].contractId").value(10))
                .andExpect(jsonPath("$.results[0].hasSignedPdf").value(true))
                .andExpect(jsonPath("$.results[1].contractId").value(20))
                .andExpect(jsonPath("$.results[1].hasSignedPdf").value(true));

        Mockito.verify(tenantService).list();
        Mockito.verify(contractFileService).hasSignedFile(10L);
        Mockito.verify(contractFileService).hasSignedFile(20L);
    }

    // ---------- POST /tenant/create ----------

    @Test
    @DisplayName("POST /tenant/create ควรเรียก service.create และคืน 201 พร้อม body")
    void create_shouldCallServiceAndReturn201() throws Exception {
        TenantDto dto = new TenantDto();
        dto.setContractId(99L);

        Mockito.when(tenantContractService.create(any(CreateTenantContractRequest.class)))
                .thenReturn(dto);

        mockMvc.perform(
                        post("/tenant/create")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}")
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.contractId").value(99));

        Mockito.verify(tenantContractService).create(any(CreateTenantContractRequest.class));
    }

    // ---------- PUT /tenant/update/{contractId} ----------

    @Test
    @DisplayName("PUT /tenant/update/{contractId} ควรเรียก service.update และคืน 200")
    void update_shouldCallServiceAndReturn200() throws Exception {
        Long contractId = 5L;
        TenantDto dto = new TenantDto();
        dto.setContractId(contractId);

        Mockito.when(tenantContractService.update(eq(contractId), any(UpdateTenantContractRequest.class)))
                .thenReturn(dto);

        mockMvc.perform(
                        put("/tenant/update/{contractId}", contractId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contractId").value(5));

        Mockito.verify(tenantContractService).update(eq(contractId), any(UpdateTenantContractRequest.class));
    }

    // ---------- DELETE /tenant/delete/{contractId} ----------

    @Test
    @DisplayName("DELETE /tenant/delete/{contractId} ควรเรียก delete ทั้ง contract และไฟล์ แล้วคืน 204")
    void delete_shouldDeleteContractAndFileAndReturn204() throws Exception {
        Long contractId = 7L;

        mockMvc.perform(delete("/tenant/delete/{contractId}", contractId))
                .andExpect(status().isNoContent());

        Mockito.verify(tenantContractService).delete(contractId);
        Mockito.verify(contractFileService).deleteByContractId(contractId);
    }

    // ---------- GET /tenant/{contractId} detail ----------

    @Test
    @DisplayName("GET /tenant/{contractId} ควรเรียก getDetail แล้วคืน 200 พร้อมข้อมูล")
    void detail_shouldReturnTenantDetail() throws Exception {
        Long contractId = 11L;
        TenantDetailDto detail = new TenantDetailDto();

        Mockito.when(tenantContractService.getDetail(contractId)).thenReturn(detail);

        mockMvc.perform(get("/tenant/{contractId}", contractId))
                .andExpect(status().isOk());

        Mockito.verify(tenantContractService).getDetail(contractId);
    }

    // ---------- GET /tenant/{contractId}/pdf (unsigned) ----------

    @Test
    @DisplayName("GET /tenant/{contractId}/pdf ควรคืน PDF และ header filename ถูกต้อง")
    void downloadContractPdf_shouldReturnPdf() throws Exception {
        Long contractId = 15L;
        byte[] pdf = new byte[]{1, 2, 3};

        Mockito.when(tenantContractService.generateContractPdf(contractId)).thenReturn(pdf);

        mockMvc.perform(get("/tenant/{contractId}/pdf", contractId))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        HttpHeaders.CONTENT_DISPOSITION,
                        containsString("tenant_" + contractId + "_contract.pdf")
                ))
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(content().bytes(pdf));

        Mockito.verify(tenantContractService).generateContractPdf(contractId);
    }

    // ---------- GET /tenant/{contractId}/pdf/signed ----------

    @Test
    @DisplayName("GET /tenant/{contractId}/pdf/signed ถ้ามีไฟล์ควรคืน 200 + PDF")
    void downloadSignedContract_whenFileExists_shouldReturnPdf() throws Exception {
        Long contractId = 20L;
        byte[] pdf = new byte[]{9, 8, 7};

        Mockito.when(contractFileService.getSignedFile(contractId)).thenReturn(pdf);

        mockMvc.perform(get("/tenant/{contractId}/pdf/signed", contractId))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        HttpHeaders.CONTENT_DISPOSITION,
                        containsString("tenant_" + contractId + "_signed.pdf")
                ))
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(content().bytes(pdf));

        Mockito.verify(contractFileService).getSignedFile(contractId);
    }

    @Test
    @DisplayName("GET /tenant/{contractId}/pdf/signed ถ้าไม่มีไฟล์ควรคืน 404 พร้อมข้อความ")
    void downloadSignedContract_whenFileMissing_shouldReturn404() throws Exception {
        Long contractId = 21L;
        Mockito.when(contractFileService.getSignedFile(contractId)).thenReturn(null);

        mockMvc.perform(get("/tenant/{contractId}/pdf/signed", contractId))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("No signed contract found")));

        Mockito.verify(contractFileService).getSignedFile(contractId);
    }

    // ---------- POST /tenant/{contractId}/pdf/upload ----------

    @Test
    @DisplayName("POST /tenant/{contractId}/pdf/upload อัปโหลดสำเร็จควรคืน 200 พร้อม message")
    void uploadSignedContract_shouldCallServiceAndReturn200() throws Exception {
        Long contractId = 30L;
        MockMultipartFile file = new MockMultipartFile(
                "file", "contract.pdf", MediaType.APPLICATION_PDF_VALUE, "dummy".getBytes());

        Mockito.doNothing()
                .when(contractFileService)
                .uploadSignedFile(eq(contractId), any(MultipartFile.class));

        mockMvc.perform(
                        multipart("/tenant/{contractId}/pdf/upload", contractId)
                                .file(file)
                )
                .andExpect(status().isOk())
                .andExpect(content().string("Signed contract uploaded successfully"));

        Mockito.verify(contractFileService)
                .uploadSignedFile(eq(contractId), any(MultipartFile.class));
    }

    @Test
    @DisplayName("POST /tenant/{contractId}/pdf/upload ถ้า service โยน exception ควรคืน 500")
    void uploadSignedContract_whenServiceThrows_shouldReturn500() throws Exception {
        Long contractId = 31L;
        MockMultipartFile file = new MockMultipartFile(
                "file", "contract.pdf", MediaType.APPLICATION_PDF_VALUE, "dummy".getBytes());

        Mockito.doThrow(new RuntimeException("disk full"))
                .when(contractFileService)
                .uploadSignedFile(eq(contractId), any(MultipartFile.class));

        mockMvc.perform(
                        multipart("/tenant/{contractId}/pdf/upload", contractId)
                                .file(file)
                )
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Error uploading file")));

        Mockito.verify(contractFileService)
                .uploadSignedFile(eq(contractId), any(MultipartFile.class));
    }

    // ---------- GET /tenant/search ----------

    @Test
    @DisplayName("GET /tenant/search ควรเรียก searchTenantWithFuzzy และใส่ hasSignedPdf ในผลลัพธ์")
    void searchTenant_shouldCallServiceAndSetHasSignedPdf() throws Exception {
        String keyword = "john";

        TenantDto t1 = new TenantDto();
        t1.setContractId(100L);

        Map<String, Object> resp = new HashMap<>();
        resp.put("results", List.of(t1));
        resp.put("total", 1);

        Mockito.when(tenantService.searchTenantWithFuzzy(keyword)).thenReturn(resp);
        Mockito.when(contractFileService.hasSignedFile(100L)).thenReturn(true);

        mockMvc.perform(get("/tenant/search").param("keyword", keyword))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.results[0].contractId").value(100))
                .andExpect(jsonPath("$.results[0].hasSignedPdf").value(true));

        Mockito.verify(tenantService).searchTenantWithFuzzy(keyword);
        Mockito.verify(contractFileService).hasSignedFile(100L);
    }
}
