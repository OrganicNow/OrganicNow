package com.organicnow.backend.service;

import com.organicnow.backend.dto.MaintenanceScheduleCreateDto;
import com.organicnow.backend.dto.MaintenanceScheduleDto;
import com.organicnow.backend.model.AssetGroup;
import com.organicnow.backend.model.MaintenanceSchedule;
import com.organicnow.backend.repository.AssetGroupRepository;
import com.organicnow.backend.repository.MaintenanceScheduleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class MaintenanceScheduleServiceTest {

    @Mock
    private MaintenanceScheduleRepository scheduleRepo;

    @Mock
    private AssetGroupRepository assetGroupRepo;

    @InjectMocks
    private MaintenanceScheduleService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // ===================================================================
    // 1) CREATE
    // ===================================================================
    @Test
    void testCreateScheduleSuccess() {
        MaintenanceScheduleCreateDto dto = new MaintenanceScheduleCreateDto();
        dto.setScheduleScope(1);
        dto.setCycleMonth(3);
        dto.setScheduleTitle("Test Maintenance");
        dto.setAssetGroupId(10L);

        AssetGroup group = AssetGroup.builder().id(10L).assetGroupName("Electrical").build();
        when(assetGroupRepo.findById(10L)).thenReturn(Optional.of(group));

        MaintenanceSchedule saved = new MaintenanceSchedule();
        saved.setId(100L);
        saved.setScheduleScope(1);
        saved.setCycleMonth(3);
        saved.setScheduleTitle("Test Maintenance");
        saved.setAssetGroup(group);

        when(scheduleRepo.save(any())).thenReturn(saved);

        MaintenanceScheduleDto result = service.createSchedule(dto);

        assertEquals(100L, result.getId());
        assertEquals("Electrical", result.getAssetGroupName());
        assertEquals(3, result.getCycleMonth());
    }

    @Test
    void testCreateScheduleAssetGroupNotFound() {

        MaintenanceScheduleCreateDto dto = new MaintenanceScheduleCreateDto();
        dto.setScheduleScope(1);
        dto.setAssetGroupId(999L);

        when(assetGroupRepo.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.createSchedule(dto));
    }

    // ===================================================================
    // 2) UPDATE
    // ===================================================================
    @Test
    void testUpdateScheduleSuccess() {

        MaintenanceSchedule existing = new MaintenanceSchedule();
        existing.setId(5L);

        when(scheduleRepo.findById(5L)).thenReturn(Optional.of(existing));

        MaintenanceScheduleCreateDto dto = new MaintenanceScheduleCreateDto();
        dto.setScheduleScope(2);
        dto.setCycleMonth(6);

        when(scheduleRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        MaintenanceScheduleDto result = service.updateSchedule(5L, dto);

        assertEquals(2, result.getScheduleScope());
        assertEquals(6, result.getCycleMonth());
    }

    @Test
    void testUpdateScheduleNotFound() {
        when(scheduleRepo.findById(99L)).thenReturn(Optional.empty());

        MaintenanceScheduleCreateDto dto = new MaintenanceScheduleCreateDto();

        assertThrows(EntityNotFoundException.class, () -> service.updateSchedule(99L, dto));
    }

    // ===================================================================
    // 3) GET ALL
    // ===================================================================
    @Test
    void testGetAllSchedules() {
        MaintenanceSchedule s1 = new MaintenanceSchedule();
        s1.setId(1L);
        s1.setScheduleTitle("A");

        when(scheduleRepo.findAll()).thenReturn(List.of(s1));

        List<MaintenanceScheduleDto> result = service.getAllSchedules();
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    // ===================================================================
    // 4) GET BY ID
    // ===================================================================
    @Test
    void testGetScheduleByIdFound() {
        MaintenanceSchedule s = new MaintenanceSchedule();
        s.setId(10L);

        when(scheduleRepo.findById(10L)).thenReturn(Optional.of(s));

        Optional<MaintenanceScheduleDto> result = service.getScheduleById(10L);
        assertTrue(result.isPresent());
    }

    @Test
    void testGetScheduleByIdNotFound() {
        when(scheduleRepo.findById(99L)).thenReturn(Optional.empty());
        assertTrue(service.getScheduleById(99L).isEmpty());
    }

    // ===================================================================
    // 5) DELETE
    // ===================================================================
    @Test
    void testDeleteScheduleSuccess() {

        MaintenanceSchedule s = new MaintenanceSchedule();
        s.setId(7L);

        when(scheduleRepo.findById(7L)).thenReturn(Optional.of(s));

        service.deleteSchedule(7L);

        verify(scheduleRepo).deleteById(7L);
    }

    @Test
    void testDeleteScheduleNotFound() {
        when(scheduleRepo.findById(88L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.deleteSchedule(88L));
    }

    // ===================================================================
    // 6) MARK AS DONE
    // ===================================================================
    @Test
    void testMarkAsDoneSuccess() {

        MaintenanceSchedule s = new MaintenanceSchedule();
        s.setId(1L);
        s.setCycleMonth(3);

        when(scheduleRepo.findById(1L)).thenReturn(Optional.of(s));
        when(scheduleRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        MaintenanceScheduleDto result = service.markAsDone(1L);

        assertNotNull(result.getLastDoneDate());
        assertNotNull(result.getNextDueDate());
    }

    @Test
    void testMarkAsDoneNotFound() {
        when(scheduleRepo.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.markAsDone(99L));
    }

    // ===================================================================
    // 7) UPCOMING SCHEDULES
    // ===================================================================
    @Test
    void testGetUpcomingSchedules() {
        LocalDateTime now = LocalDateTime.now();

        MaintenanceSchedule s = new MaintenanceSchedule();
        s.setId(12L);
        s.setNextDueDate(now.plusDays(2));

        when(scheduleRepo.findByNextDueDateBetween(any(), any()))
                .thenReturn(List.of(s));

        List<MaintenanceScheduleDto> result = service.getUpcomingSchedules(5);

        assertEquals(1, result.size());
        assertEquals(12L, result.get(0).getId());
    }
}
