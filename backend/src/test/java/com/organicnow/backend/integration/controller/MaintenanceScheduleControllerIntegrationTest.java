package com.organicnow.backend.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.organicnow.backend.BackendApplication;
import com.organicnow.backend.dto.MaintenanceScheduleCreateDto;
import com.organicnow.backend.dto.MaintenanceScheduleDto;
import com.organicnow.backend.service.AssetGroupService;
import com.organicnow.backend.service.MaintenanceScheduleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = BackendApplication.class)
@AutoConfigureMockMvc(addFilters = false)
class MaintenanceScheduleControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    MaintenanceScheduleService scheduleService;

    @MockBean
    AssetGroupService assetGroupService;

    // -------------------------------------------------------
    // 1) POST /schedules  (create)
    // -------------------------------------------------------
    @Test
    void create_shouldReturnSchedule_whenServiceSuccess() throws Exception {
        MaintenanceScheduleCreateDto payload = new MaintenanceScheduleCreateDto();

        MaintenanceScheduleDto dto = new MaintenanceScheduleDto();
        when(scheduleService.createSchedule(any(MaintenanceScheduleCreateDto.class)))
                .thenReturn(dto);

        mockMvc.perform(
                        post("/schedules")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(payload))
                )
                .andExpect(status().isOk());
    }

    // -------------------------------------------------------
    // 2) PUT /schedules/{id}  (update)
    // -------------------------------------------------------
    @Test
    void update_shouldReturnSchedule_whenServiceSuccess() throws Exception {
        Long id = 1L;
        MaintenanceScheduleCreateDto payload = new MaintenanceScheduleCreateDto();

        MaintenanceScheduleDto dto = new MaintenanceScheduleDto();
        when(scheduleService.updateSchedule(eq(id), any(MaintenanceScheduleCreateDto.class)))
                .thenReturn(dto);

        mockMvc.perform(
                        put("/schedules/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(payload))
                )
                .andExpect(status().isOk());
    }

    // -------------------------------------------------------
    // 3) GET /schedules  (getAll + assetGroupDropdown)
    // -------------------------------------------------------
    @Test
    void getAll_shouldReturnResultAndAssetGroupDropdown() throws Exception {
        List<MaintenanceScheduleDto> schedules = List.of(
                new MaintenanceScheduleDto(),
                new MaintenanceScheduleDto()
        );

        when(scheduleService.getAllSchedules()).thenReturn(schedules);
        // ไม่ต้องการยุ่งกับชนิดจริงของ dropdown -> คืน list ว่างพอ
        when(assetGroupService.getAllGroupsForDropdown()).thenReturn(List.of());

        mockMvc.perform(get("/schedules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.length()").value(2))
                .andExpect(jsonPath("$.assetGroupDropdown.length()").value(0));
    }

    // -------------------------------------------------------
    // 4) GET /schedules/{id}
    // -------------------------------------------------------
    @Test
    void getById_shouldReturnSchedule_whenFound() throws Exception {
        Long id = 10L;
        MaintenanceScheduleDto dto = new MaintenanceScheduleDto();
        when(scheduleService.getScheduleById(id))
                .thenReturn(Optional.of(dto));

        mockMvc.perform(get("/schedules/{id}", id))
                .andExpect(status().isOk());
    }

    @Test
    void getById_shouldReturn404_whenNotFound() throws Exception {
        Long id = 999L;
        when(scheduleService.getScheduleById(id))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/schedules/{id}", id))
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------
    // 5) DELETE /schedules/{id}
    // -------------------------------------------------------
    @Test
    void delete_shouldReturn204_andCallService() throws Exception {
        Long id = 5L;
        doNothing().when(scheduleService).deleteSchedule(id);

        mockMvc.perform(delete("/schedules/{id}", id))
                .andExpect(status().isNoContent());

        verify(scheduleService).deleteSchedule(id);
    }

    // -------------------------------------------------------
    // 6) PATCH /schedules/{id}/done
    // -------------------------------------------------------
    @Test
    void markAsDone_shouldReturnUpdatedSchedule() throws Exception {
        Long id = 7L;
        MaintenanceScheduleDto dto = new MaintenanceScheduleDto();
        when(scheduleService.markAsDone(id))
                .thenReturn(dto);

        mockMvc.perform(patch("/schedules/{id}/done", id))
                .andExpect(status().isOk());
    }

    // -------------------------------------------------------
    // 7) GET /schedules/upcoming (default days=7)
    // -------------------------------------------------------
    @Test
    void getUpcoming_shouldUseDefaultDaysAndReturnResponse() throws Exception {
        List<MaintenanceScheduleDto> schedules = List.of(
                new MaintenanceScheduleDto()
        );
        when(scheduleService.getUpcomingSchedules(7))
                .thenReturn(schedules);
        when(assetGroupService.getAllGroupsForDropdown()).thenReturn(List.of());

        mockMvc.perform(get("/schedules/upcoming"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.length()").value(1))
                .andExpect(jsonPath("$.assetGroupDropdown.length()").value(0));
    }

    // -------------------------------------------------------
    // 8) GET /schedules/upcoming?days=X (custom days)
    // -------------------------------------------------------
    @Test
    void getUpcoming_withCustomDays_shouldPassDaysToService() throws Exception {
        int days = 3;

        List<MaintenanceScheduleDto> schedules = List.of(
                new MaintenanceScheduleDto(),
                new MaintenanceScheduleDto()
        );
        when(scheduleService.getUpcomingSchedules(days))
                .thenReturn(schedules);
        when(assetGroupService.getAllGroupsForDropdown()).thenReturn(List.of());

        mockMvc.perform(get("/schedules/upcoming")
                        .param("days", String.valueOf(days)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.length()").value(2))
                .andExpect(jsonPath("$.assetGroupDropdown.length()").value(0))
                .andExpect(content().string(containsString("result")))
                .andExpect(content().string(containsString("assetGroupDropdown")));

        verify(scheduleService).getUpcomingSchedules(days);
    }
}
