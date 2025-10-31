///* Copyright (C) 2025 Kemisara Anankamongkol - All Rights Reserved
// * You may use, distribute and modify this code under the terms of the MIT license.
// */
//package com.organicnow.backend.controller;
//
//import com.organicnow.backend.dto.MaintenanceScheduleCreateDto;
//import com.organicnow.backend.dto.MaintenanceScheduleDto;
//import com.organicnow.backend.dto.MaintenanceScheduleResponse;
//import com.organicnow.backend.service.AssetGroupService;
//import com.organicnow.backend.service.MaintenanceScheduleService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.http.ResponseEntity;
//
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//class MaintenanceScheduleControllerTest {
//
//    @Mock
//    private MaintenanceScheduleService scheduleService;
//
//    @Mock
//    private AssetGroupService assetGroupService;
//
//    @InjectMocks
//    private MaintenanceScheduleController scheduleController;
//
//    private MaintenanceScheduleDto sampleSchedule;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//
//        sampleSchedule = new MaintenanceScheduleDto();
//        try {
//            sampleSchedule.getClass().getMethod("setId", Long.class).invoke(sampleSchedule, 1L);
//        } catch (Exception ignored) {}
//        try {
//            sampleSchedule.getClass().getMethod("setTitle", String.class).invoke(sampleSchedule, "Aircon Cleaning");
//        } catch (Exception ignored) {}
//    }
//
//    @Test
//    void testCreateSchedule() {
//        MaintenanceScheduleCreateDto dto = new MaintenanceScheduleCreateDto();
//        when(scheduleService.createSchedule(dto)).thenReturn(sampleSchedule);
//
//        ResponseEntity<MaintenanceScheduleDto> response = scheduleController.create(dto);
//
//        assertEquals(200, response.getStatusCode().value());
//        assertEquals(sampleSchedule, response.getBody());
//        verify(scheduleService).createSchedule(dto);
//    }
//
//    @Test
//    void testUpdateSchedule() {
//        MaintenanceScheduleCreateDto dto = new MaintenanceScheduleCreateDto();
//        when(scheduleService.updateSchedule(1L, dto)).thenReturn(sampleSchedule);
//
//        ResponseEntity<MaintenanceScheduleDto> response = scheduleController.update(1L, dto);
//
//        assertEquals(200, response.getStatusCode().value());
//        assertEquals(sampleSchedule, response.getBody());
//        verify(scheduleService).updateSchedule(1L, dto);
//    }
//
//    @Test
//    void testGetAllSchedules() {
//        when(scheduleService.getAllSchedules()).thenReturn(List.of(sampleSchedule));
//        when(assetGroupService.getAllGroupsForDropdown()).thenReturn((List) List.of("Group A", "Group B"));
//
//        ResponseEntity<MaintenanceScheduleResponse> response = scheduleController.getAll();
//
//        assertEquals(200, response.getStatusCode().value());
//        assertNotNull(response.getBody());
//        assertEquals(1, response.getBody().getResult().size());
//        assertEquals(2, response.getBody().getAssetGroupDropdown().size());
//
//        verify(scheduleService).getAllSchedules();
//        verify(assetGroupService).getAllGroupsForDropdown();
//    }
//
//    @Test
//    void testGetByIdFound() {
//        when(scheduleService.getScheduleById(1L)).thenReturn(Optional.of(sampleSchedule));
//
//        ResponseEntity<MaintenanceScheduleDto> response = scheduleController.getById(1L);
//
//        assertEquals(200, response.getStatusCode().value());
//        assertEquals(sampleSchedule, response.getBody());
//        verify(scheduleService).getScheduleById(1L);
//    }
//
//    @Test
//    void testGetByIdNotFound() {
//        when(scheduleService.getScheduleById(99L)).thenReturn(Optional.empty());
//
//        ResponseEntity<MaintenanceScheduleDto> response = scheduleController.getById(99L);
//
//        assertEquals(404, response.getStatusCode().value());
//        verify(scheduleService).getScheduleById(99L);
//    }
//
//    @Test
//    void testDeleteSchedule() {
//        doNothing().when(scheduleService).deleteSchedule(1L);
//
//        ResponseEntity<Void> response = scheduleController.delete(1L);
//
//        assertEquals(204, response.getStatusCode().value());
//        verify(scheduleService).deleteSchedule(1L);
//    }
//
//    @Test
//    void testMarkAsDone() {
//        when(scheduleService.markAsDone(1L)).thenReturn(sampleSchedule);
//
//        ResponseEntity<MaintenanceScheduleDto> response = scheduleController.markAsDone(1L);
//
//        assertEquals(200, response.getStatusCode().value());
//        assertEquals(sampleSchedule, response.getBody());
//        verify(scheduleService).markAsDone(1L);
//    }
//
//    @Test
//    void testGetUpcomingSchedules() {
//        when(scheduleService.getUpcomingSchedules(10)).thenReturn(List.of(sampleSchedule));
//        // ✅ mock ให้คืนค่า 2 รายการ
//        when(assetGroupService.getAllGroupsForDropdown()).thenReturn((List) List.of("Group A", "Group B"));
//
//        ResponseEntity<MaintenanceScheduleResponse> response = scheduleController.getUpcoming(10);
//
//        assertEquals(200, response.getStatusCode().value());
//        assertNotNull(response.getBody());
//        assertEquals(1, response.getBody().getResult().size());
//        // ✅ ปรับ expected จาก 1 → 2 ให้ตรงกับ mock
//        assertEquals(2, response.getBody().getAssetGroupDropdown().size());
//
//        verify(scheduleService).getUpcomingSchedules(10);
//        verify(assetGroupService).getAllGroupsForDropdown();
//    }
//}
//
