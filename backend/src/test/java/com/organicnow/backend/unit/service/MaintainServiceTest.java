package com.organicnow.backend.unit.service;

import com.organicnow.backend.dto.CreateMaintainRequest;
import com.organicnow.backend.dto.MaintainDto;
import com.organicnow.backend.dto.UpdateMaintainRequest;
import com.organicnow.backend.service.MaintainServiceImpl;
import com.organicnow.backend.model.*;
import com.organicnow.backend.repository.MaintainRepository;
import com.organicnow.backend.repository.RoomAssetRepository;
import com.organicnow.backend.repository.RoomRepository;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class MaintainServiceTest {

    @Mock
    private MaintainRepository maintainRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private RoomAssetRepository roomAssetRepository;

    @InjectMocks
    private MaintainServiceImpl maintainService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // =====================================================================================
    // ✅ 1) getAll()
    // =====================================================================================
    @Test
    void testGetAllSuccess() {
        Maintain m = Maintain.builder()
                .id(1L)
                .issueTitle("Leak")
                .issueCategory(1)
                .createDate(LocalDateTime.now())
                .build();

        when(maintainRepository.findAll()).thenReturn(List.of(m));

        List<MaintainDto> result = maintainService.getAll();
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    // =====================================================================================
    // ✅ 2) getById()
    // =====================================================================================
    @Test
    void testGetByIdFound() {
        Maintain m = Maintain.builder().id(10L).issueTitle("Broken Light").build();
        when(maintainRepository.findById(10L)).thenReturn(Optional.of(m));

        Optional<MaintainDto> result = maintainService.getById(10L);
        assertTrue(result.isPresent());
        assertEquals(10L, result.get().getId());
    }

    @Test
    void testGetByIdNotFound() {
        when(maintainRepository.findById(99L)).thenReturn(Optional.empty());
        assertTrue(maintainService.getById(99L).isEmpty());
    }

    // =====================================================================================
    // ✅ 3) create()
    // =====================================================================================
    @Test
    void testCreateSuccess() {
        // mock room
        Room room = Room.builder().id(5L).roomNumber("201").roomFloor(2).build();
        when(roomRepository.findById(5L)).thenReturn(Optional.of(room));

        // mock asset
        RoomAsset asset = RoomAsset.builder().id(7L).build();
        when(roomAssetRepository.findById(7L)).thenReturn(Optional.of(asset));

        CreateMaintainRequest req = new CreateMaintainRequest();
        req.setTargetType(1);
        req.setRoomId(5L);
        req.setRoomAssetId(7L);
        req.setIssueCategory(2);
        req.setIssueTitle("Water leak");

        Maintain saved = Maintain.builder()
                .id(100L)
                .room(room)
                .roomAsset(asset)
                .issueTitle("Water leak")
                .build();

        when(maintainRepository.save(any())).thenReturn(saved);

        MaintainDto dto = maintainService.create(req);

        assertEquals(100L, dto.getId());
        assertEquals("Water leak", dto.getIssueTitle());
        assertEquals(5L, dto.getRoomId());
        assertEquals(7L, dto.getRoomAssetId());
    }

    // ❌ targetType missing
    @Test
    void testCreateFailMissingTargetType() {
        CreateMaintainRequest req = new CreateMaintainRequest();
        req.setIssueCategory(1);
        req.setIssueTitle("Leak");

        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> maintainService.create(req));
        assertTrue(ex.getMessage().contains("targetType is required"));
    }

    // ❌ roomId missing
    @Test
    void testCreateFailMissingRoom() {
        CreateMaintainRequest req = new CreateMaintainRequest();
        req.setTargetType(1);
        req.setIssueCategory(1);
        req.setIssueTitle("Leak");

        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> maintainService.create(req));
        assertTrue(ex.getMessage().contains("roomId or roomNumber is required"));
    }

    // ❌ issueTitle missing
    @Test
    void testCreateFailMissingIssueTitle() {
        CreateMaintainRequest req = new CreateMaintainRequest();
        req.setTargetType(1);
        req.setRoomId(5L);
        req.setIssueCategory(1);

        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> maintainService.create(req));
        assertTrue(ex.getMessage().contains("issueTitle is required"));
    }

    // =====================================================================================
    // ✅ 4) update()
    // =====================================================================================
    @Test
    void testUpdateSuccess() {
        Maintain existing = Maintain.builder()
                .id(1L)
                .issueTitle("Old Title")
                .build();

        when(maintainRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(maintainRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        UpdateMaintainRequest req = new UpdateMaintainRequest();
        req.setIssueTitle("New Title");

        MaintainDto dto = maintainService.update(1L, req);

        assertEquals("New Title", dto.getIssueTitle());
    }

    @Test
    void testUpdateNotFound() {
        when(maintainRepository.findById(99L)).thenReturn(Optional.empty());

        UpdateMaintainRequest req = new UpdateMaintainRequest();

        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> maintainService.update(99L, req));

        assertTrue(ex.getMessage().contains("Maintain not found"));
    }

    // =====================================================================================
    // ✅ 5) delete()
    // =====================================================================================
    @Test
    void testDeleteExists() {
        when(maintainRepository.existsById(10L)).thenReturn(true);
        maintainService.delete(10L);
        verify(maintainRepository).deleteById(10L);
    }

    @Test
    void testDeleteNotExists() {
        when(maintainRepository.existsById(10L)).thenReturn(false);
        maintainService.delete(10L);
        verify(maintainRepository, never()).deleteById(anyLong());
    }

    // =====================================================================================
    // ✅ 6) generateMaintenanceReportPdf()
    // =====================================================================================
    @Test
    void testGeneratePdfSuccess() {
        Room room = Room.builder()
                .id(5L)
                .roomFloor(2)
                .roomNumber("201")
                .build();

        Maintain m = Maintain.builder()
                .id(1L)
                .room(room)
                .targetType(0)              // 👈 เพิ่มบรรทัดนี้ (0 = Item Repair, 1 = Room Repair ตามโค้ด)
                .issueTitle("Leak")
                .issueCategory(1)
                .createDate(LocalDateTime.now())
                .build();

        when(maintainRepository.findById(1L)).thenReturn(Optional.of(m));

        byte[] pdf = maintainService.generateMaintenanceReportPdf(1L);

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }


    @Test
    void testGeneratePdfMaintainNotFound() {
        when(maintainRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> maintainService.generateMaintenanceReportPdf(99L));
    }

    @Test
    void testGeneratePdfRoomMissing() {
        Maintain m = Maintain.builder().id(1L).build(); // ❌ ไม่มี room

        when(maintainRepository.findById(1L)).thenReturn(Optional.of(m));

        Exception ex = assertThrows(RuntimeException.class,
                () -> maintainService.generateMaintenanceReportPdf(1L));
        assertTrue(ex.getMessage().contains("Room not found"));
    }
}
