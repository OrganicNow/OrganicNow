package com.organicnow.backend.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.organicnow.backend.BackendApplication;
import com.organicnow.backend.dto.CreateMaintainRequest;
import com.organicnow.backend.dto.MaintainDto;
import com.organicnow.backend.dto.UpdateMaintainRequest;
import com.organicnow.backend.service.MaintainRoomService;
import com.organicnow.backend.service.MaintainService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = BackendApplication.class)
@AutoConfigureMockMvc(addFilters = false)
class MaintainControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    MaintainRoomService maintainRoomService;

    @MockBean
    MaintainService maintainService;

    // -------------------------------------------------------
    // 1) GET /maintain/{roomId}/requests
    // -------------------------------------------------------
    @Test
    void getRequestsByRoom_shouldReturnSuccessStatus() throws Exception {
        Long roomId = 1L;

        // คืน list ว่าง ๆ พอ เพื่อให้ controller ทำงานปกติ
        when(maintainRoomService.getRequestsByRoomId(roomId))
                .thenReturn(List.of());

        mockMvc.perform(get("/maintain/{roomId}/requests", roomId))
                .andExpect(status().isOk())
                // เช็คแค่ว่ามี status = "success"
                .andExpect(jsonPath("$.status").value("success"));
        // ไม่เช็ค $.data.length() แล้ว เพราะ field data ไม่มีใน response จริง
    }

    // -------------------------------------------------------
    // 2) GET /maintain/list
    // -------------------------------------------------------
    @Test
    void list_shouldReturnAllMaintains() throws Exception {
        when(maintainService.getAll())
                .thenReturn(List.of(
                        mock(MaintainDto.class),
                        mock(MaintainDto.class)
                ));

        mockMvc.perform(get("/maintain/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    // -------------------------------------------------------
    // 3) GET /maintain/{id}
    // -------------------------------------------------------
    @Test
    void get_whenFound_shouldReturnMaintain() throws Exception {
        Long id = 1L;
        when(maintainService.getById(id))
                .thenReturn(Optional.of(mock(MaintainDto.class)));

        mockMvc.perform(get("/maintain/{id}", id))
                .andExpect(status().isOk());
    }

    @Test
    void get_whenNotFound_shouldReturn404() throws Exception {
        Long id = 999L;
        when(maintainService.getById(id))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/maintain/{id}", id))
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------
    // 4) POST /maintain/create
    // -------------------------------------------------------
    @Test
    void create_whenSuccess_shouldReturn200() throws Exception {
        CreateMaintainRequest req = new CreateMaintainRequest();
        when(maintainService.create(any(CreateMaintainRequest.class)))
                .thenReturn(mock(MaintainDto.class));

        mockMvc.perform(
                        post("/maintain/create")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req))
                )
                .andExpect(status().isOk());
    }

    @Test
    void create_whenServiceThrows_shouldReturn400WithMessage() throws Exception {
        CreateMaintainRequest req = new CreateMaintainRequest();
        when(maintainService.create(any(CreateMaintainRequest.class)))
                .thenThrow(new RuntimeException("Boom"));

        mockMvc.perform(
                        post("/maintain/create")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req))
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Create failed")));
    }

    // -------------------------------------------------------
    // 5) PUT /maintain/update/{id}
    // -------------------------------------------------------
    @Test
    void update_whenSuccess_shouldReturn200() throws Exception {
        Long id = 1L;
        UpdateMaintainRequest req = new UpdateMaintainRequest();
        when(maintainService.update(eq(id), any(UpdateMaintainRequest.class)))
                .thenReturn(mock(MaintainDto.class));

        mockMvc.perform(
                        put("/maintain/update/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req))
                )
                .andExpect(status().isOk());
    }

    @Test
    void update_whenServiceThrows_shouldReturn400() throws Exception {
        Long id = 1L;
        UpdateMaintainRequest req = new UpdateMaintainRequest();
        when(maintainService.update(eq(id), any(UpdateMaintainRequest.class)))
                .thenThrow(new RuntimeException("Update error"));

        mockMvc.perform(
                        put("/maintain/update/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req))
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Update failed")));
    }

    // -------------------------------------------------------
    // 6) DELETE /maintain/{id}
    // -------------------------------------------------------
    @Test
    void delete_whenSuccess_shouldReturn200() throws Exception {
        Long id = 1L;
        doNothing().when(maintainService).delete(id);

        mockMvc.perform(delete("/maintain/{id}", id))
                .andExpect(status().isOk());
    }

    @Test
    void delete_whenServiceThrows_shouldReturn400() throws Exception {
        Long id = 1L;
        doThrow(new RuntimeException("Delete error"))
                .when(maintainService).delete(id);

        mockMvc.perform(delete("/maintain/{id}", id))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Delete failed")));
    }

    // -------------------------------------------------------
    // 7) GET /maintain/{id}/report-pdf
    // -------------------------------------------------------
    @Test
    void generateMaintenanceReportPdf_whenSuccess_shouldReturnPdfBytes() throws Exception {
        Long id = 1L;
        byte[] pdfBytes = "dummy-pdf".getBytes();
        when(maintainService.generateMaintenanceReportPdf(id))
                .thenReturn(pdfBytes);

        mockMvc.perform(get("/maintain/{id}/report-pdf", id))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().string("Content-Disposition",
                        org.hamcrest.Matchers.containsString("maintenance-report-" + id + ".pdf")))
                .andExpect(content().bytes(pdfBytes));
    }

    @Test
    void generateMaintenanceReportPdf_whenException_shouldReturn400() throws Exception {
        Long id = 1L;
        when(maintainService.generateMaintenanceReportPdf(id))
                .thenThrow(new RuntimeException("PDF error"));

        mockMvc.perform(get("/maintain/{id}/report-pdf", id))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------
    // 8) POST /maintain/{maintainId}/work-image
    // -------------------------------------------------------
    @Test
    void uploadWorkImage_whenValidImage_shouldReturn200AndUrl() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "photo.png",
                "image/png",
                "dummy-image".getBytes()
        );

        mockMvc.perform(
                        multipart("/maintain/{id}/work-image", 10L)
                                .file(file)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").isNotEmpty())
                .andExpect(jsonPath("$.filename").isNotEmpty())
                .andExpect(jsonPath("$.message").value("อัพโหลดรูปภาพสำเร็จ"));
    }

    @Test
    void uploadWorkImage_whenFileEmpty_shouldReturn400() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "empty.png",
                "image/png",
                new byte[0]
        );

        mockMvc.perform(
                        multipart("/maintain/{id}/work-image", 11L)
                                .file(file)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("ไม่มีไฟล์ที่อัพโหลด"));
    }

    @Test
    void uploadWorkImage_whenNotImage_shouldReturn400() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "doc.txt",
                "text/plain",
                "not-an-image".getBytes()
        );

        mockMvc.perform(
                        multipart("/maintain/{id}/work-image", 12L)
                                .file(file)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("ไฟล์ต้องเป็นรูปภาพเท่านั้น"));
    }
}
