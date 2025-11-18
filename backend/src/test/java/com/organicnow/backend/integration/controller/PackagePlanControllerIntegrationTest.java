package com.organicnow.backend.integration.controller;

import com.organicnow.backend.controller.PackagePlanController;
import com.organicnow.backend.dto.PackagePlanDto;
import com.organicnow.backend.dto.PackagePlanRequestDto;
import com.organicnow.backend.service.PackagePlanService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PackagePlanController.class)
@AutoConfigureMockMvc(addFilters = false)
class PackagePlanControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    PackagePlanService packagePlanService;

    // -------------------------------------------------------
    // 1) GET /packages - list ทั้งหมด
    // -------------------------------------------------------
    @Test
    void listPackages_shouldReturn200AndArray() throws Exception {
        PackagePlanDto dto1 = Mockito.mock(PackagePlanDto.class);
        PackagePlanDto dto2 = Mockito.mock(PackagePlanDto.class);

        Mockito.when(packagePlanService.getAllPackages())
                .thenReturn(List.of(dto1, dto2));

        mockMvc.perform(get("/packages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void listPackages_whenServiceThrows_shouldReturn500() throws Exception {
        Mockito.when(packagePlanService.getAllPackages())
                .thenThrow(new RuntimeException("boom"));

        mockMvc.perform(get("/packages"))
                .andExpect(status().isInternalServerError());
    }

    // -------------------------------------------------------
    // 2) POST /packages - สร้าง package ใหม่
    // -------------------------------------------------------
    @Test
    void createPackage_withValidBody_shouldReturn201AndCallService() throws Exception {
        // ✅ ใส่ isActive ด้วย เพื่อให้ผ่าน validation
        String json = """
                {
                  "roomSize": 30,
                  "price": 5000,
                  "contractTypeId": 1,
                  "isActive": 1
                }
                """;

        mockMvc.perform(
                        post("/packages")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isCreated());

        verify(packagePlanService).createPackage(any(PackagePlanRequestDto.class));
    }

    @Test
    void createPackage_whenServiceThrows_shouldReturn500() throws Exception {
        // ✅ body ต้อง valid เช่นกัน เพื่อให้ถึง service แล้วค่อย throw
        String json = """
                {
                  "roomSize": 30,
                  "price": 5000,
                  "contractTypeId": 1,
                  "isActive": 1
                }
                """;

        doThrow(new RuntimeException("save error"))
                .when(packagePlanService).createPackage(any(PackagePlanRequestDto.class));

        mockMvc.perform(
                        post("/packages")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isInternalServerError());
    }

    // invalid body → 400 จาก @Valid + RestExceptionHandler
    @Test
    void createPackage_withInvalidBody_shouldReturn400() throws Exception {
        String json = "{}";

        mockMvc.perform(
                        post("/packages")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------
    // 3) PATCH /packages/{id}/toggle - สลับ isActive
    // -------------------------------------------------------
    @Test
    void toggleStatus_shouldReturn200AndBody() throws Exception {
        Long id = 10L;
        PackagePlanDto dto = Mockito.mock(PackagePlanDto.class);

        Mockito.when(packagePlanService.togglePackageStatus(eq(id)))
                .thenReturn(dto);

        mockMvc.perform(patch("/packages/{id}/toggle", id))
                .andExpect(status().isOk());
    }

    @Test
    void toggleStatus_whenServiceThrows_shouldReturn500() throws Exception {
        Long id = 99L;

        Mockito.when(packagePlanService.togglePackageStatus(eq(id)))
                .thenThrow(new RuntimeException("toggle failed"));

        mockMvc.perform(patch("/packages/{id}/toggle", id))
                .andExpect(status().isInternalServerError());
    }
}
