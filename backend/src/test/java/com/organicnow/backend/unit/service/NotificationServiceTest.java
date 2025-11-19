package com.organicnow.backend.unit.service;

import com.organicnow.backend.dto.NotificationDueDto;
import com.organicnow.backend.model.MaintenanceNotificationSkip;
import com.organicnow.backend.model.MaintenanceSchedule;
import com.organicnow.backend.repository.MaintenanceNotificationSkipRepository;
import com.organicnow.backend.repository.MaintenanceScheduleRepository;
import com.organicnow.backend.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.*;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class NotificationServiceTest {

    private MaintenanceScheduleRepository scheduleRepo;
    private MaintenanceNotificationSkipRepository skipRepo;
    private NotificationServiceImpl service;

    private final ZoneId ZONE_TH = ZoneId.of("Asia/Bangkok");

    @BeforeEach
    void setUp() {
        scheduleRepo = mock(MaintenanceScheduleRepository.class);
        skipRepo = mock(MaintenanceNotificationSkipRepository.class);
        service = new NotificationServiceImpl(scheduleRepo, skipRepo);
    }

    // -------------------------------------------------------
    // ✅ TEST: getDueNotifications()
    // -------------------------------------------------------
    @Test
    void testGetDueNotifications_returnsNotification_whenTodayReachedNotifyWindow() {

        // today = 2025-01-10
        LocalDate today = LocalDate.of(2025, 1, 10);
        Clock fixedClock = Clock.fixed(today.atStartOfDay(ZONE_TH).toInstant(), ZONE_TH);
        LocalDateTime fixedNow = LocalDateTime.ofInstant(fixedClock.instant(), ZONE_TH);

        // Prepare Schedule
        MaintenanceSchedule s = new MaintenanceSchedule();
        s.setId(1L);
        s.setNotifyBeforeDate(3);  // 3 days before
        s.setScheduleTitle("Filter Cleaning");

        s.setLastDoneDate(null);
        s.setNextDueDate(LocalDateTime.of(2025, 1, 12, 0, 0)); // due 2 days from today

        when(scheduleRepo.findAll()).thenReturn(List.of(s));

        // No skip
        when(skipRepo.existsByScheduleIdAndDueDate(1L, LocalDate.of(2025, 1, 12)))
                .thenReturn(false);

        List<NotificationDueDto> result = service.getDueNotifications(99L);

        assertEquals(1, result.size());
        NotificationDueDto dto = result.get(0);

        assertEquals(1L, dto.getScheduleId());
        assertEquals(LocalDate.of(2025, 1, 12), dto.getNextDueDate());
        assertEquals("Filter Cleaning", dto.getTitle());
    }

    @Test
    void testGetDueNotifications_skippedSchedule_shouldNotReturn() {

        MaintenanceSchedule s = new MaintenanceSchedule();
        s.setId(2L);
        s.setNotifyBeforeDate(0);
        s.setNextDueDate(LocalDateTime.of(2025, 1, 15, 0, 0));

        when(scheduleRepo.findAll()).thenReturn(List.of(s));

        // User already skipped this schedule+dueDate
        when(skipRepo.existsByScheduleIdAndDueDate(2L, LocalDate.of(2025, 1, 15)))
                .thenReturn(true);

        List<NotificationDueDto> result = service.getDueNotifications(99L);

        assertEquals(0, result.size());
    }

    // -------------------------------------------------------
    // ✅ TEST: skipScheduleDue()
    // -------------------------------------------------------
    @Test
    void testSkipScheduleDue_savesNewSkip_whenNotExists() {

        LocalDate dueDate = LocalDate.of(2025, 2, 1);

        when(skipRepo.existsByScheduleIdAndDueDate(5L, dueDate))
                .thenReturn(false);

        service.skipScheduleDue(5L, dueDate);

        ArgumentCaptor<MaintenanceNotificationSkip> captor =
                ArgumentCaptor.forClass(MaintenanceNotificationSkip.class);

        verify(skipRepo, times(1)).save(captor.capture());

        MaintenanceNotificationSkip saved = captor.getValue();

        assertEquals(5L, saved.getScheduleId());
        assertEquals(dueDate, saved.getDueDate());
    }

    @Test
    void testSkipScheduleDue_doesNotSave_ifAlreadyExists() {

        LocalDate dueDate = LocalDate.of(2025, 2, 1);

        when(skipRepo.existsByScheduleIdAndDueDate(5L, dueDate))
                .thenReturn(true); // already exists

        service.skipScheduleDue(5L, dueDate);

        verify(skipRepo, never()).save(any());
    }

    // -------------------------------------------------------
    // ✅ TEST: skipScheduleDueDate() -> delegates to skipScheduleDue()
    // -------------------------------------------------------
    @Test
    void testSkipScheduleDueDate_callsSkipScheduleDue() {

        LocalDate dueDate = LocalDate.of(2025, 3, 10);

        when(skipRepo.existsByScheduleIdAndDueDate(7L, dueDate))
                .thenReturn(false);

        service.skipScheduleDueDate(7L, dueDate);

        verify(skipRepo, times(1)).save(any(MaintenanceNotificationSkip.class));
    }

    // -------------------------------------------------------
    // ✅ TEST: checkAndCreateDueNotifications() — no-op
    // -------------------------------------------------------
    @Test
    void testCheckAndCreateDueNotifications_doesNothing() {
        // Should NOT call any repo
        service.checkAndCreateDueNotifications();
        verifyNoInteractions(scheduleRepo, skipRepo);
    }
}
