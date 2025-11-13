//package com.organicnow.backend.unit.service;
//
//import com.organicnow.backend.dto.CreateMaintainRequest;
//import com.organicnow.backend.dto.MaintainDto;
//import com.organicnow.backend.dto.UpdateMaintainRequest;
//import com.organicnow.backend.model.Maintain;
//import com.organicnow.backend.model.Room;
//import com.organicnow.backend.model.RoomAsset;
//import com.organicnow.backend.repository.MaintainRepository;
//import com.organicnow.backend.repository.RoomAssetRepository;
//import com.organicnow.backend.repository.RoomRepository;
//import com.organicnow.backend.service.MaintainServiceImpl;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.time.LocalDateTime;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyLong;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class MaintainServicelTest {
//
//    @Mock
//    private MaintainRepository maintainRepository;
//
//    @Mock
//    private RoomRepository roomRepository;
//
//    @Mock
//    private RoomAssetRepository roomAssetRepository;
//
//    @InjectMocks
//    private MaintainServiceImpl maintainService;
//
//    private Room testRoom;
//    private RoomAsset testAsset;
//    private Maintain testMaintain;
//    private LocalDateTime now;
//
//    @BeforeEach
//    void setUp() {
//        now = LocalDateTime.now();
//
//        testRoom = new Room();
//        testRoom.setId(1L);
//        testRoom.setRoomNumber("101");
//        testRoom.setRoomFloor(1);
//
//        testAsset = new RoomAsset();
//        testAsset.setId(1L);
//
//        testMaintain = Maintain.builder()
//                .id(1L)
//                .targetType(1)  // 1 = ROOM, 2 = ASSET
//                .room(testRoom)
//                .roomAsset(testAsset)
//                .issueCategory(1)  // 1 = ELECTRICAL, 2 = PLUMBING, 3 = HEATING
//                .issueTitle("AC not working")
//                .issueDescription("The air conditioner is not cooling")
//                .createDate(now)
//                .scheduledDate(now.plusDays(1))
//                .finishDate(null)
//                .build();
//    }
//
//    @Test
//    void getAll_ShouldReturnAllMaintainRecords() {
//        // Arrange
//        Maintain maintain2 = Maintain.builder()
//                .id(2L)
//                .targetType(2)  // ASSET
//                .room(testRoom)
//                .issueCategory(2)  // PLUMBING
//                .issueTitle("Leak")
//                .issueDescription("Water leak")
//                .createDate(now)
//                .build();
//
//        when(maintainRepository.findAll()).thenReturn(Arrays.asList(testMaintain, maintain2));
//
//        // Act
//        List<MaintainDto> result = maintainService.getAll();
//
//        // Assert
//        assertEquals(2, result.size());
//        assertEquals(1L, result.get(0).getId());
//        assertEquals(1, result.get(0).getTargetType());
//        assertEquals(2L, result.get(1).getId());
//        verify(maintainRepository, times(1)).findAll();
//    }
//
//    @Test
//    void getById_WhenExists_ShouldReturnMaintainDto() {
//        // Arrange
//        when(maintainRepository.findById(1L)).thenReturn(Optional.of(testMaintain));
//
//        // Act
//        Optional<MaintainDto> result = maintainService.getById(1L);
//
//        // Assert
//        assertTrue(result.isPresent());
//        assertEquals(1L, result.get().getId());
//        assertEquals("AC not working", result.get().getIssueTitle());
//        assertEquals("101", result.get().getRoomNumber());
//        verify(maintainRepository, times(1)).findById(1L);
//    }
//
//    @Test
//    void getById_WhenNotExists_ShouldReturnEmpty() {
//        // Arrange
//        when(maintainRepository.findById(999L)).thenReturn(Optional.empty());
//
//        // Act
//        Optional<MaintainDto> result = maintainService.getById(999L);
//
//        // Assert
//        assertFalse(result.isPresent());
//        verify(maintainRepository, times(1)).findById(999L);
//    }
//
//    @Test
//    void create_WithRoomId_ShouldCreateSuccessfully() {
//        // Arrange
//        CreateMaintainRequest req = new CreateMaintainRequest();
//        req.setTargetType(1);  // ROOM
//        req.setRoomId(1L);
//        req.setIssueCategory(1);  // ELECTRICAL
//        req.setIssueTitle("AC not working");
//        req.setIssueDescription("The air conditioner is not cooling");
//        req.setCreateDate(now);
//        req.setScheduledDate(now.plusDays(1));
//
//        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
//        when(maintainRepository.save(any(Maintain.class))).thenReturn(testMaintain);
//
//        // Act
//        MaintainDto result = maintainService.create(req);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals("AC not working", result.getIssueTitle());
//        assertEquals("101", result.getRoomNumber());
//        verify(roomRepository, times(1)).findById(1L);
//        verify(maintainRepository, times(1)).save(any(Maintain.class));
//    }
//
//    @Test
//    void create_WithRoomNumber_ShouldCreateSuccessfully() {
//        // Arrange
//        CreateMaintainRequest req = new CreateMaintainRequest();
//        req.setTargetType(1);  // ROOM
//        req.setRoomNumber("101");
//        req.setIssueCategory(1);  // ELECTRICAL
//        req.setIssueTitle("AC not working");
//
//        when(roomRepository.findByRoomNumber("101")).thenReturn(Optional.of(testRoom));
//        when(maintainRepository.save(any(Maintain.class))).thenReturn(testMaintain);
//
//        // Act
//        MaintainDto result = maintainService.create(req);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals("101", result.getRoomNumber());
//        verify(roomRepository, times(1)).findByRoomNumber("101");
//        verify(maintainRepository, times(1)).save(any(Maintain.class));
//    }
//
//    @Test
//    void create_WithRoomAsset_ShouldCreateSuccessfully() {
//        // Arrange
//        CreateMaintainRequest req = new CreateMaintainRequest();
//        req.setTargetType(2);  // ASSET
//        req.setRoomId(1L);
//        req.setRoomAssetId(1L);
//        req.setIssueCategory(1);  // ELECTRICAL
//        req.setIssueTitle("AC not working");
//
//        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
//        when(roomAssetRepository.findById(1L)).thenReturn(Optional.of(testAsset));
//        when(maintainRepository.save(any(Maintain.class))).thenReturn(testMaintain);
//
//        // Act
//        MaintainDto result = maintainService.create(req);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals(1L, result.getRoomAssetId());
//        verify(roomAssetRepository, times(1)).findById(1L);
//    }
//
//    @Test
//    void update_AllFields_ShouldUpdateSuccessfully() {
//        // Arrange
//        Room newRoom = new Room();
//        newRoom.setId(2L);
//        newRoom.setRoomNumber("202");
//        newRoom.setRoomFloor(2);
//
//        RoomAsset newAsset = new RoomAsset();
//        newAsset.setId(2L);
//
//        UpdateMaintainRequest req = new UpdateMaintainRequest();
//        req.setTargetType(2);  // 2 = ASSET
//        req.setRoomId(2L);
//        req.setRoomAssetId(2L);
//        req.setIssueCategory(3);  // 3 = HEATING
//        req.setIssueTitle("Heater broken");
//        req.setIssueDescription("Not heating properly");
//        req.setScheduledDate(now.plusDays(2));
//        req.setFinishDate(now.plusDays(3));
//
//        when(maintainRepository.findById(1L)).thenReturn(Optional.of(testMaintain));
//        when(roomRepository.findById(2L)).thenReturn(Optional.of(newRoom));
//        when(roomAssetRepository.findById(2L)).thenReturn(Optional.of(newAsset));
//        when(maintainRepository.save(any(Maintain.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//        // Act
//        MaintainDto result = maintainService.update(1L, req);
//
//        // Assert
//        assertNotNull(result);
//        verify(maintainRepository, times(1)).findById(1L);
//        verify(maintainRepository, times(1)).save(any(Maintain.class));
//    }
//
//    @Test
//    void update_PartialFields_ShouldUpdateOnlyProvidedFields() {
//        // Arrange
//        UpdateMaintainRequest req = new UpdateMaintainRequest();
//        req.setIssueTitle("Updated Title");
//        req.setFinishDate(now.plusDays(5));
//
//        when(maintainRepository.findById(1L)).thenReturn(Optional.of(testMaintain));
//        when(maintainRepository.save(any(Maintain.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//        // Act
//        MaintainDto result = maintainService.update(1L, req);
//
//        // Assert
//        assertNotNull(result);
//        verify(maintainRepository, times(1)).save(any(Maintain.class));
//    }
//
//    @Test
//    void update_WithRoomNumber_ShouldResolveRoom() {
//        // Arrange
//        Room newRoom = new Room();
//        newRoom.setId(2L);
//        newRoom.setRoomNumber("202");
//        newRoom.setRoomFloor(2);
//
//        UpdateMaintainRequest req = new UpdateMaintainRequest();
//        req.setRoomNumber("202");
//
//        when(maintainRepository.findById(1L)).thenReturn(Optional.of(testMaintain));
//        when(roomRepository.findByRoomNumber("202")).thenReturn(Optional.of(newRoom));
//        when(maintainRepository.save(any(Maintain.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//        // Act
//        MaintainDto result = maintainService.update(1L, req);
//
//        // Assert
//        assertNotNull(result);
//        verify(roomRepository, times(1)).findByRoomNumber("202");
//    }
//
//    @Test
//    void update_NonExistentMaintain_ShouldThrowException() {
//        // Arrange
//        UpdateMaintainRequest req = new UpdateMaintainRequest();
//        req.setIssueTitle("Updated Title");
//
//        when(maintainRepository.findById(999L)).thenReturn(Optional.empty());
//
//        // Act & Assert
//        IllegalArgumentException exception = assertThrows(
//                IllegalArgumentException.class,
//                () -> maintainService.update(999L, req)
//        );
//        assertEquals("Maintain not found: 999", exception.getMessage());
//    }
//
//    @Test
//    void delete_ExistingMaintain_ShouldDeleteSuccessfully() {
//        // Arrange
//        when(maintainRepository.existsById(1L)).thenReturn(true);
//
//        // Act
//        maintainService.delete(1L);
//
//        // Assert
//        verify(maintainRepository, times(1)).existsById(1L);
//        verify(maintainRepository, times(1)).deleteById(1L);
//    }
//
//    @Test
//    void delete_NonExistentMaintain_ShouldNotThrowException() {
//        // Arrange
//        when(maintainRepository.existsById(999L)).thenReturn(false);
//
//        // Act
//        maintainService.delete(999L);
//
//        // Assert
//        verify(maintainRepository, times(1)).existsById(999L);
//        verify(maintainRepository, never()).deleteById(anyLong());
//    }
//}
