//package com.organicnow.backend.unit.service;
//
//import com.organicnow.backend.dto.MaintenanceScheduleCreateDto;
//import com.organicnow.backend.dto.MaintenanceScheduleDto;
//import com.organicnow.backend.model.AssetGroup;
//import com.organicnow.backend.model.MaintenanceSchedule;
//import com.organicnow.backend.repository.AssetGroupRepository;
//import com.organicnow.backend.repository.MaintenanceScheduleRepository;
//import com.organicnow.backend.service.MaintenanceScheduleService;
//import jakarta.persistence.EntityNotFoundException;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//
//import java.time.LocalDateTime;
//import java.time.temporal.ChronoUnit;
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//class MaintenanceScheduleServiceTest {
//
//    @Mock
//    private MaintenanceScheduleRepository scheduleRepository;
//
//    @Mock
//    private AssetGroupRepository assetGroupRepository;
//
//    @InjectMocks
//    private MaintenanceScheduleService scheduleService;
//
//    private MaintenanceSchedule schedule;
//    private MaintenanceScheduleCreateDto createDto;
//    private AssetGroup assetGroup;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//
//        // --- mock AssetGroup ---
//        assetGroup = new AssetGroup();
//        try {
//            assetGroup.getClass().getMethod("setId", Long.class).invoke(assetGroup, 11L);
//            // ถ้า field ชื่อ assetGroupName:
//            assetGroup.getClass().getMethod("setAssetGroupName", String.class).invoke(assetGroup, "HVAC");
//        } catch (Exception ignored) {}
//
//        // --- entity ตัวอย่างที่ repo จะคืนกลับ ---
//        schedule = new MaintenanceSchedule();
//        try {
//            schedule.getClass().getMethod("setId", Long.class).invoke(schedule, 100L);
//        } catch (Exception ignored) {}
//
//        schedule.setScheduleScope(0); // 0 = Asset (ตามที่คุณใช้)
//        schedule.setAssetGroup(assetGroup);
//        schedule.setCycleMonth(6);
//        schedule.setScheduleTitle("AC Cleaning");
//        schedule.setScheduleDescription("Clean air conditioner filters");
//        schedule.setLastDoneDate(LocalDateTime.now().minusMonths(6).truncatedTo(ChronoUnit.SECONDS));
//        schedule.setNextDueDate(LocalDateTime.now().plusMonths(6).truncatedTo(ChronoUnit.SECONDS));
//        schedule.setNotifyBeforeDate(3);
//
//        // --- DTO ที่ส่งเข้า service ---
//        createDto = new MaintenanceScheduleCreateDto();
//        try {
//            // ให้ตรงกับ service: ใช้ assetGroupId (ไม่ใช้ room/roomAsset)
//            createDto.getClass().getMethod("setAssetGroupId", Long.class).invoke(createDto, 11L);
//            createDto.getClass().getMethod("setScheduleScope", Integer.class).invoke(createDto, 0);
//            createDto.getClass().getMethod("setCycleMonth", Integer.class).invoke(createDto, 6);
//            createDto.getClass().getMethod("setScheduleTitle", String.class).invoke(createDto, "AC Cleaning");
//            createDto.getClass().getMethod("setScheduleDescription", String.class).invoke(createDto, "Clean air conditioner filters");
//            createDto.getClass().getMethod("setNotifyBeforeDate", Integer.class).invoke(createDto, 3);
//            // เผื่อ DTO มีฟิลด์ last/next (optinal)
//            try { createDto.getClass().getMethod("setLastDoneDate", LocalDateTime.class).invoke(createDto, null); } catch (Exception ignored) {}
//            try { createDto.getClass().getMethod("setNextDueDate", LocalDateTime.class).invoke(createDto, null); } catch (Exception ignored) {}
//        } catch (Exception ignored) {}
//    }
//
//    @Test
//    void testCreateSchedule() {
//        when(assetGroupRepository.findById(11L)).thenReturn(Optional.of(assetGroup));
//        when(scheduleRepository.save(any(MaintenanceSchedule.class))).thenReturn(schedule);
//
//        MaintenanceScheduleDto result = scheduleService.createSchedule(createDto);
//
//        assertNotNull(result);
//        assertEquals("AC Cleaning", result.getScheduleTitle());
//        assertEquals(11L, result.getAssetGroupId());
//        assertEquals("HVAC", result.getAssetGroupName());
//        verify(assetGroupRepository, times(1)).findById(11L);
//        verify(scheduleRepository, times(1)).save(any(MaintenanceSchedule.class));
//    }
//
//    @Test
//    void testCreateSchedule_AssetGroupNotFound() {
//        when(assetGroupRepository.findById(11L)).thenReturn(Optional.empty());
//        assertThrows(EntityNotFoundException.class, () -> scheduleService.createSchedule(createDto));
//        verify(assetGroupRepository, times(1)).findById(11L);
//        verify(scheduleRepository, never()).save(any());
//    }
//
//    @Test
//    void testUpdateSchedule() {
//        when(scheduleRepository.findById(100L)).thenReturn(Optional.of(schedule));
//        when(assetGroupRepository.findById(11L)).thenReturn(Optional.of(assetGroup));
//        when(scheduleRepository.save(any(MaintenanceSchedule.class))).thenReturn(schedule);
//
//        MaintenanceScheduleDto result = scheduleService.updateSchedule(100L, createDto);
//
//        assertNotNull(result);
//        assertEquals("AC Cleaning", result.getScheduleTitle());
//        assertEquals(11L, result.getAssetGroupId());
//        verify(scheduleRepository, times(1)).findById(100L);
//        verify(assetGroupRepository, times(1)).findById(11L);
//        verify(scheduleRepository, times(1)).save(any(MaintenanceSchedule.class));
//    }
//
//    @Test
//    void testUpdateSchedule_NotFound() {
//        when(scheduleRepository.findById(999L)).thenReturn(Optional.empty());
//        assertThrows(EntityNotFoundException.class, () -> scheduleService.updateSchedule(999L, createDto));
//        verify(scheduleRepository, times(1)).findById(999L);
//        verify(scheduleRepository, never()).save(any());
//    }
//
//    @Test
//    void testGetAllSchedules() {
//        when(scheduleRepository.findAll()).thenReturn(List.of(schedule));
//        List<MaintenanceScheduleDto> result = scheduleService.getAllSchedules();
//
//        assertEquals(1, result.size());
//        assertEquals("AC Cleaning", result.get(0).getScheduleTitle());
//        assertEquals("HVAC", result.get(0).getAssetGroupName());
//        verify(scheduleRepository, times(1)).findAll();
//    }
//
//    @Test
//    void testGetScheduleById() {
//        when(scheduleRepository.findById(100L)).thenReturn(Optional.of(schedule));
//
//        Optional<MaintenanceScheduleDto> result = scheduleService.getScheduleById(100L);
//
//        assertTrue(result.isPresent());
//        assertEquals("AC Cleaning", result.get().getScheduleTitle());
//        verify(scheduleRepository, times(1)).findById(100L);
//    }
//
//    @Test
//    void testDeleteSchedule() {
//        // Mock การค้นหาข้อมูลใน scheduleRepository
//        when(scheduleRepository.findById(100L)).thenReturn(Optional.of(schedule));
//
//        // Mock การลบใน scheduleRepository
//        doNothing().when(scheduleRepository).deleteById(100L);
//
//        // ทดสอบฟังก์ชัน deleteSchedule
//        scheduleService.deleteSchedule(100L);
//
//        // ตรวจสอบว่า method deleteById ถูกเรียกเพียงครั้งเดียว
//        verify(scheduleRepository, times(1)).deleteById(100L);
//    }
//
//
//    @Test
//    void testMarkAsDone() {
//        when(scheduleRepository.findById(100L)).thenReturn(Optional.of(schedule));
//        when(scheduleRepository.save(any(MaintenanceSchedule.class))).thenReturn(schedule);
//
//        MaintenanceScheduleDto result = scheduleService.markAsDone(100L);
//
//        assertNotNull(result);
//        assertNotNull(result.getLastDoneDate());
//        // ถ้ามี cycleMonth > 0 ต้องมี nextDueDate
//        assertNotNull(result.getNextDueDate());
//        verify(scheduleRepository, times(1)).save(any(MaintenanceSchedule.class));
//    }
//
//    @Test
//    void testMarkAsDone_NotFound() {
//        when(scheduleRepository.findById(999L)).thenReturn(Optional.empty());
//        assertThrows(EntityNotFoundException.class, () -> scheduleService.markAsDone(999L));
//    }
//
//    @Test
//    void testGetUpcomingSchedules() {
//        when(scheduleRepository.findByNextDueDateBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
//                .thenReturn(List.of(schedule));
//
//        List<MaintenanceScheduleDto> result = scheduleService.getUpcomingSchedules(7);
//
//        assertEquals(1, result.size());
//        verify(scheduleRepository, times(1))
//                .findByNextDueDateBetween(any(LocalDateTime.class), any(LocalDateTime.class));
//    }
//}
//
////package com.organicnow.backend.service;
////
////import com.organicnow.backend.dto.MaintenanceScheduleCreateDto;
////import com.organicnow.backend.dto.MaintenanceScheduleDto;
////import com.organicnow.backend.model.MaintenanceSchedule;
////import com.organicnow.backend.model.Room;
////import com.organicnow.backend.model.RoomAsset;
////import com.organicnow.backend.repository.MaintenanceScheduleRepository;
////import com.organicnow.backend.repository.RoomAssetRepository;
////import com.organicnow.backend.repository.RoomRepository;
////import org.junit.jupiter.api.BeforeEach;
////import org.junit.jupiter.api.Test;
////import org.mockito.InjectMocks;
////import org.mockito.Mock;
////import org.mockito.MockitoAnnotations;
////
////import java.time.LocalDateTime;
////import java.util.List;
////import java.util.Optional;
////
////import static org.junit.jupiter.api.Assertions.*;
////import static org.mockito.Mockito.*;
////
////class MaintenanceScheduleServiceTest {
////
////    @Mock
////    private MaintenanceScheduleRepository scheduleRepository;
////
////    @Mock
////    private RoomRepository roomRepository;
////
////    @Mock
////    private RoomAssetRepository roomAssetRepository;
////
////    @InjectMocks
////    private MaintenanceScheduleService scheduleService;
////
////    private MaintenanceSchedule schedule;
////    private MaintenanceScheduleCreateDto createDto;
////    private Room room;
////    private RoomAsset roomAsset;
////
////    @BeforeEach
////    void setUp() {
////        MockitoAnnotations.openMocks(this);
////
////        room = new Room();
////        try {
////            room.getClass().getMethod("setId", Long.class).invoke(room, 1L);
////        } catch (Exception ignored) {}
////
////        roomAsset = new RoomAsset();
////        try {
////            roomAsset.getClass().getMethod("setId", Long.class).invoke(roomAsset, 10L);
////        } catch (Exception ignored) {}
////
////        schedule = MaintenanceSchedule.builder()
////                .id(100L)
////                .scheduleScope(0) // ✅ Integer (0 = room)
////                .room(room)
////                .roomAsset(roomAsset)
////                .cycleMonth(6)
////                .scheduleTitle("AC Cleaning")
////                .scheduleDescription("Clean air conditioner filters")
////                .lastDoneDate(LocalDateTime.now().minusMonths(6))
////                .nextDueDate(LocalDateTime.now().plusMonths(6))
////                .notifyBeforeDate(3) // ✅ ใช้ Integer แทน LocalDateTime
////                .build();
////
////        createDto = new MaintenanceScheduleCreateDto();
////        try {
////            createDto.getClass().getMethod("setRoomId", Long.class).invoke(createDto, 1L);
////            createDto.getClass().getMethod("setRoomAssetId", Long.class).invoke(createDto, 10L);
////            createDto.getClass().getMethod("setScheduleScope", Integer.class).invoke(createDto, 0);
////            createDto.getClass().getMethod("setCycleMonth", Integer.class).invoke(createDto, 6);
////            createDto.getClass().getMethod("setScheduleTitle", String.class).invoke(createDto, "AC Cleaning");
////            createDto.getClass().getMethod("setScheduleDescription", String.class).invoke(createDto, "Clean air conditioner filters");
////            createDto.getClass().getMethod("setNotifyBeforeDate", Integer.class).invoke(createDto, 3); // ✅ Integer
////        } catch (Exception ignored) {}
////    }
////
////    @Test
////    void testCreateSchedule() {
////        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
////        when(roomAssetRepository.findById(10L)).thenReturn(Optional.of(roomAsset));
////        when(scheduleRepository.save(any(MaintenanceSchedule.class))).thenReturn(schedule);
////
////        MaintenanceScheduleDto result = scheduleService.createSchedule(createDto);
////
////        assertNotNull(result);
////        assertEquals("AC Cleaning", result.getScheduleTitle());
////        verify(scheduleRepository, times(1)).save(any(MaintenanceSchedule.class));
////    }
////
////    @Test
////    void testUpdateSchedule() {
////        when(scheduleRepository.findById(100L)).thenReturn(Optional.of(schedule));
////        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
////        when(roomAssetRepository.findById(10L)).thenReturn(Optional.of(roomAsset));
////        when(scheduleRepository.save(any(MaintenanceSchedule.class))).thenReturn(schedule);
////
////        MaintenanceScheduleDto result = scheduleService.updateSchedule(100L, createDto);
////
////        assertNotNull(result);
////        assertEquals("AC Cleaning", result.getScheduleTitle());
////        verify(scheduleRepository, times(1)).save(any(MaintenanceSchedule.class));
////    }
////
////    @Test
////    void testUpdateSchedule_NotFound() {
////        when(scheduleRepository.findById(999L)).thenReturn(Optional.empty());
////        assertThrows(RuntimeException.class, () -> scheduleService.updateSchedule(999L, createDto));
////    }
////
////    @Test
////    void testGetAllSchedules() {
////        when(scheduleRepository.findAll()).thenReturn(List.of(schedule));
////
////        List<MaintenanceScheduleDto> result = scheduleService.getAllSchedules();
////
////        assertEquals(1, result.size());
////        assertEquals("AC Cleaning", result.get(0).getScheduleTitle());
////        verify(scheduleRepository, times(1)).findAll();
////    }
////
////    @Test
////    void testGetScheduleById() {
////        when(scheduleRepository.findById(100L)).thenReturn(Optional.of(schedule));
////
////        Optional<MaintenanceScheduleDto> result = scheduleService.getScheduleById(100L);
////
////        assertTrue(result.isPresent());
////        assertEquals("AC Cleaning", result.get().getScheduleTitle());
////        verify(scheduleRepository, times(1)).findById(100L);
////    }
////
////    @Test
////    void testDeleteSchedule() {
////        doNothing().when(scheduleRepository).deleteById(100L);
////        scheduleService.deleteSchedule(100L);
////        verify(scheduleRepository, times(1)).deleteById(100L);
////    }
////
////    @Test
////    void testMarkAsDone() {
////        when(scheduleRepository.findById(100L)).thenReturn(Optional.of(schedule));
////        when(scheduleRepository.save(any(MaintenanceSchedule.class))).thenReturn(schedule);
////
////        MaintenanceScheduleDto result = scheduleService.markAsDone(100L);
////
////        assertNotNull(result);
////        assertNotNull(result.getLastDoneDate());
////        assertNotNull(result.getNextDueDate());
////        verify(scheduleRepository, times(1)).save(any(MaintenanceSchedule.class));
////    }
////
////    @Test
////    void testMarkAsDone_NotFound() {
////        when(scheduleRepository.findById(999L)).thenReturn(Optional.empty());
////        assertThrows(RuntimeException.class, () -> scheduleService.markAsDone(999L));
////    }
////
////    @Test
////    void testGetUpcomingSchedules() {
////        when(scheduleRepository.findByNextDueDateBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
////                .thenReturn(List.of(schedule));
////
////        List<MaintenanceScheduleDto> result = scheduleService.getUpcomingSchedules(7);
////
////        assertEquals(1, result.size());
////        verify(scheduleRepository, times(1))
////                .findByNextDueDateBetween(any(LocalDateTime.class), any(LocalDateTime.class));
////    }
////}
