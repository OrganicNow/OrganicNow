package com.organicnow.backend.unit.service;

import com.organicnow.backend.dto.AssetDto;
import com.organicnow.backend.dto.AssetEventDto;
import com.organicnow.backend.dto.RequestDto;
import com.organicnow.backend.dto.RoomDetailDto;
import com.organicnow.backend.dto.RoomUpdateDto;
import com.organicnow.backend.model.Asset;
import com.organicnow.backend.model.AssetEvent;
import com.organicnow.backend.model.Room;
import com.organicnow.backend.model.RoomAsset;
import com.organicnow.backend.repository.AssetEventRepository;
import com.organicnow.backend.repository.AssetRepository;
import com.organicnow.backend.repository.MaintainRepository;
import com.organicnow.backend.repository.RoomAssetRepository;
import com.organicnow.backend.repository.RoomRepository;
import com.organicnow.backend.service.RoomService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mockito;


import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.anyList;


@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private MaintainRepository maintainRepository;

    @Mock
    private RoomAssetRepository roomAssetRepository;

    @Mock
    private AssetEventRepository assetEventRepository;

    @InjectMocks
    private RoomService roomService;

    // ============================================================
    // ✅ getAllRooms()
    // ============================================================
    @Test
    void getAllRooms_ShouldReturnRoomsWithAssetsAndRequestsAndMappedSize() {

        // ==== Mock room entity ====
        Room roomEntity = Room.builder()
                .id(1L)
                .roomNumber("101")
                .roomFloor(1)
                .roomSize(0)        // Studio
                .build();

        when(roomRepository.findById(1L))
                .thenReturn(Optional.of(roomEntity));

        // ==== Mock RoomDetailDto returned from findAllRooms() ====
        RoomDetailDto dto = new RoomDetailDto(
                1L,
                "101",
                1,
                "occupied",
                "John",
                "Doe",
                "0900000000",
                "john@example.com",
                "Standard",
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(roomRepository.findAllRooms()).thenReturn(List.of(dto));

        // ==== Mock Asset Rows (จาก roomAssetRepository.findAssetsByRoomIds) ====
        Object[][] rows = new Object[][] {
                {
                        1L,          // roomId
                        10L,         // assetId
                        "Aircon",    // assetName
                        "Electrical",// groupName
                        1,           // roomFloor
                        "101"        // roomNumber
                }
        };

// ⭐ FIX: cast rows[0] ให้เป็น Object[]
        List<Object[]> assetRows = Collections.singletonList((Object[]) rows[0]);

        when(roomAssetRepository.findAssetsByRoomIds(Mockito.<List<Long>>eq(List.of(1L))))
                .thenReturn(assetRows);







        // ==== Mock Requests ====
        RequestDto req = new RequestDto(
                100L,
                "Fix AC",
                "description",   // ⭐ ใส่ description ให้ถูกตำแหน่ง
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );


        when(maintainRepository.findRequestsByRoomId(1L))
                .thenReturn(List.of(req));

        // ----------------------------------------------------------------------

        List<RoomDetailDto> result = roomService.getAllRooms();

        // ----------------------------------------------------------------------

        assertEquals(1, result.size());
        RoomDetailDto r = result.get(1 - 1);

        assertEquals("101", r.getRoomNumber());
        assertEquals("Studio", r.getRoomSize());  // mapped correctly

        // assets mapped?
        assertEquals(1, r.getAssets().size());
        assertEquals("Aircon", r.getAssets().get(0).getAssetName());

        // requests mapped?
        assertEquals(1, r.getRequests().size());
        assertEquals("Fix AC", r.getRequests().get(0).getIssueTitle());
    }
    @Test
    void getAllRooms_WhenNoRooms_ShouldReturnEmptyList() {
        when(roomRepository.findAllRooms()).thenReturn(Collections.emptyList());

        List<RoomDetailDto> result = roomService.getAllRooms();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(roomAssetRepository, never()).findAssetsByRoomIds(anyList());
    }

    // ============================================================
    // ✅ getRoomDetail()
    // ============================================================
    @Test
    void getRoomDetail_WhenFound_ShouldReturnWithAssetsRequestsAndSize() {
        Long roomId = 1L;

        RoomDetailDto dto = new RoomDetailDto(
                roomId, "202", 2, "occupied",
                "Jane", "Smith", "555", "x@y.com",
                "Yearly", LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now()
        );
        when(roomRepository.findRoomDetail(roomId)).thenReturn(dto);

        Room roomEntity = Room.builder()
                .id(roomId)
                .roomNumber("202")
                .roomFloor(2)
                .roomSize(0) // Studio
                .build();
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(roomEntity));

        List<AssetDto> assets = List.of(
                new AssetDto(10L, "Table", "Furniture", 2, "202")
        );
        when(assetRepository.findAssetsByRoomId(roomId)).thenReturn(assets);

        RequestDto req = new RequestDto(
                1L, "Leak", LocalDateTime.now(), null
        );
        when(maintainRepository.findRequestsByRoomId(roomId))
                .thenReturn(List.of(req));

        RoomDetailDto result = roomService.getRoomDetail(roomId);

        assertNotNull(result);
        assertEquals("202", result.getRoomNumber());
        assertEquals("Studio", result.getRoomSize());
        assertEquals(1, result.getAssets().size());
        assertEquals("Table", result.getAssets().get(0).getAssetName());
        assertEquals(1, result.getRequests().size());
        assertEquals("Leak", result.getRequests().get(0).getIssueTitle());
    }

    @Test
    void getRoomDetail_WhenNotFound_ShouldReturnNull() {
        when(roomRepository.findRoomDetail(1L)).thenReturn(null);

        RoomDetailDto result = roomService.getRoomDetail(1L);

        assertNull(result);
        verify(roomRepository, never()).findById(anyLong());
    }

    // ============================================================
    // ✅ addAssetToRoom()
    // ============================================================
    @Test
    void addAssetToRoom_Success_ShouldCreateRoomAssetAndSetStatusInUse() {
        Long roomId = 1L;
        Long assetId = 10L;

        when(roomAssetRepository.existsByRoomIdAndAssetId(roomId, assetId))
                .thenReturn(false);

        Room room = Room.builder()
                .id(roomId)
                .roomNumber("101")
                .roomFloor(1)
                .roomSize(0)
                .build();
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));

        Asset asset = Asset.builder()
                .id(assetId)
                .assetName("Aircon")
                .status("available")
                .build();
        when(assetRepository.findAvailableById(assetId)).thenReturn(asset);

        roomService.addAssetToRoom(roomId, assetId);

        ArgumentCaptor<RoomAsset> raCaptor = ArgumentCaptor.forClass(RoomAsset.class);
        verify(roomAssetRepository).save(raCaptor.capture());
        RoomAsset saved = raCaptor.getValue();
        assertEquals(room, saved.getRoom());
        assertEquals(asset, saved.getAsset());

        ArgumentCaptor<Asset> assetCaptor = ArgumentCaptor.forClass(Asset.class);
        verify(assetRepository).save(assetCaptor.capture());
        assertEquals("in_use", assetCaptor.getValue().getStatus());
    }

    @Test
    void addAssetToRoom_WhenAssetAlreadyExists_ShouldThrow() {
        when(roomAssetRepository.existsByRoomIdAndAssetId(1L, 2L))
                .thenReturn(true);

        assertThrows(RuntimeException.class,
                () -> roomService.addAssetToRoom(1L, 2L));
        verify(roomRepository, never()).findById(anyLong());
    }

    @Test
    void addAssetToRoom_WhenRoomNotFound_ShouldThrow() {
        when(roomAssetRepository.existsByRoomIdAndAssetId(1L, 2L))
                .thenReturn(false);
        when(roomRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> roomService.addAssetToRoom(1L, 2L));
    }

    @Test
    void addAssetToRoom_WhenAssetNotAvailable_ShouldThrow() {
        Room room = Room.builder()
                .id(1L)
                .roomNumber("101")
                .roomFloor(1)
                .roomSize(0)
                .build();

        when(roomAssetRepository.existsByRoomIdAndAssetId(1L, 2L))
                .thenReturn(false);
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(assetRepository.findAvailableById(2L)).thenReturn(null);

        assertThrows(RuntimeException.class,
                () -> roomService.addAssetToRoom(1L, 2L));
    }

    // ============================================================
    // ✅ removeAssetFromRoom()
    // ============================================================
    @Test
    void removeAssetFromRoom_Success_ShouldDeleteRelationAndSetAssetAvailable() {
        Long roomId = 1L;
        Long assetId = 2L;

        Asset asset = Asset.builder()
                .id(assetId)
                .assetName("Chair")
                .status("in_use")
                .build();
        RoomAsset ra = RoomAsset.builder()
                .id(100L)
                .room(Room.builder().id(roomId).build())
                .asset(asset)
                .build();

        when(roomAssetRepository.findByRoomIdAndAssetId(roomId, assetId))
                .thenReturn(Optional.of(ra));
        when(assetRepository.findById(assetId)).thenReturn(Optional.of(asset));

        roomService.removeAssetFromRoom(roomId, assetId);

        verify(roomAssetRepository).delete(ra);
        ArgumentCaptor<Asset> captor = ArgumentCaptor.forClass(Asset.class);
        verify(assetRepository).save(captor.capture());
        assertEquals("available", captor.getValue().getStatus());
    }

    @Test
    void removeAssetFromRoom_WhenRelationNotFound_ShouldThrow() {
        when(roomAssetRepository.findByRoomIdAndAssetId(1L, 2L))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> roomService.removeAssetFromRoom(1L, 2L));
    }

    // ============================================================
    // ✅ updateRoomAssets()
    // ============================================================
    @Test
    void updateRoomAssets_ShouldRemoveAndAddAndUpdateStatuses() {
        Long roomId = 1L;

        Room room = Room.builder()
                .id(roomId)
                .roomNumber("101")
                .roomFloor(1)
                .roomSize(0)
                .build();
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));

        Asset a1 = Asset.builder().id(1L).assetName("Old1").status("in_use").build();
        Asset a2 = Asset.builder().id(2L).assetName("Old2").status("in_use").build();

        RoomAsset ra1 = RoomAsset.builder().id(101L).room(room).asset(a1).build();
        RoomAsset ra2 = RoomAsset.builder().id(102L).room(room).asset(a2).build();

        when(roomAssetRepository.findByRoomId(roomId))
                .thenReturn(List.of(ra1, ra2));

        // new assetIds = [2,3]  => remove 1, add 3
        List<Long> newIds = List.of(2L, 3L);

        Asset a3 = Asset.builder().id(3L).assetName("New3").status("available").build();
        when(assetRepository.findAllById(Set.of(3L))).thenReturn(List.of(a3));

        roomService.updateRoomAssets(roomId, newIds);

        // asset 1 -> available
        ArgumentCaptor<Asset> assetCaptor = ArgumentCaptor.forClass(Asset.class);
        verify(assetRepository, atLeastOnce()).save(assetCaptor.capture());
        List<Asset> savedAssets = assetCaptor.getAllValues();

        boolean old1Available = savedAssets.stream()
                .anyMatch(a -> a.getId().equals(1L) && "available".equals(a.getStatus()));
        boolean new3InUse = savedAssets.stream()
                .anyMatch(a -> a.getId().equals(3L) && "in_use".equals(a.getStatus()));

        assertTrue(old1Available);
        assertTrue(new3InUse);

        // new RoomAsset created for asset 3
        ArgumentCaptor<RoomAsset> raCaptor = ArgumentCaptor.forClass(RoomAsset.class);
        verify(roomAssetRepository, atLeastOnce()).save(raCaptor.capture());
        RoomAsset newRa = raCaptor.getValue();
        assertEquals(a3, newRa.getAsset());
        assertEquals(room, newRa.getRoom());
    }

    // ============================================================
    // ✅ updateRoomAssetsWithReason()
    // ============================================================
    @Test
    void updateRoomAssetsWithReason_ShouldLogEventsForAddedAndRemoved() {
        Long roomId = 1L;
        String reasonType = "addon";
        String note = "change assets";

        Room room = Room.builder()
                .id(roomId)
                .roomNumber("201")
                .roomFloor(2)
                .roomSize(1)
                .build();
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));

        Asset a1 = Asset.builder().id(1L).assetName("Desk").status("in_use").build();
        Asset a2 = Asset.builder().id(2L).assetName("Lamp").status("in_use").build();

        RoomAsset ra1 = RoomAsset.builder().id(201L).room(room).asset(a1).build();
        RoomAsset ra2 = RoomAsset.builder().id(202L).room(room).asset(a2).build();

        when(roomAssetRepository.findByRoomId(roomId))
                .thenReturn(List.of(ra1, ra2));

        // new = [2,3] => remove 1, add 3
        List<Long> newIds = List.of(2L, 3L);

        Asset a3 = Asset.builder().id(3L).assetName("Chair").status("available").build();
        when(assetRepository.findAllById(Set.of(3L))).thenReturn(List.of(a3));

        roomService.updateRoomAssetsWithReason(roomId, newIds, reasonType, note);

        // event สำหรับ removed (asset 1) และ added (asset 3)
        ArgumentCaptor<AssetEvent> eventCaptor = ArgumentCaptor.forClass(AssetEvent.class);
        verify(assetEventRepository, atLeast(2)).save(eventCaptor.capture());
        List<AssetEvent> events = eventCaptor.getAllValues();

        boolean removedLogged = events.stream().anyMatch(e ->
                "removed".equals(e.getEventType())
                        && e.getAsset().getId().equals(1L)
                        && reasonType.equals(e.getReasonType())
                        && note.equals(e.getNote())
        );

        boolean addedLogged = events.stream().anyMatch(e ->
                "added".equals(e.getEventType())
                        && e.getAsset().getId().equals(3L)
                        && reasonType.equals(e.getReasonType())
                        && note.equals(e.getNote())
        );

        assertTrue(removedLogged);
        assertTrue(addedLogged);
    }

    // ============================================================
    // ✅ getRoomAssetEvents()
    // ============================================================
    @Test
    void getRoomAssetEvents_ShouldMapEntityToDto() {
        Room room = Room.builder().id(1L).roomNumber("101").roomFloor(1).roomSize(0).build();
        Asset asset = Asset.builder().id(10L).assetName("TV").status("in_use").build();

        AssetEvent event = AssetEvent.builder()
                .eventId(100L)
                .room(room)
                .asset(asset)
                .eventType("added")
                .reasonType("addon")
                .note("new asset")
                .createdAt(LocalDateTime.now())
                .build();

        when(assetEventRepository.findByRoom_Id(1L))
                .thenReturn(List.of(event));

        List<AssetEventDto> result = roomService.getRoomAssetEvents(1L);

        assertEquals(1, result.size());
        AssetEventDto dto = result.get(0);
        assertEquals(100L, dto.getEventId());
        assertEquals(1L, dto.getRoomId());
        assertEquals(10L, dto.getAssetId());
        assertEquals("TV", dto.getAssetName());
        assertEquals("added", dto.getEventType());
        assertEquals("addon", dto.getReasonType());
        assertEquals("new asset", dto.getNote());
        assertNotNull(dto.getCreatedAt());
    }

    // ============================================================
    // ✅ updateRoom()
    // ============================================================
    @Test
    void updateRoom_Success_ShouldUpdateFieldsAndMapSizeFromName() {
        Room room = Room.builder()
                .id(1L)
                .roomNumber("101")
                .roomFloor(1)
                .roomSize(0)
                .build();
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        RoomUpdateDto dto = RoomUpdateDto.builder()
                .roomFloor(3)
                .roomNumber("305")
                .roomSize("Deluxe") // => 2
                .build();

        roomService.updateRoom(1L, dto);

        ArgumentCaptor<Room> captor = ArgumentCaptor.forClass(Room.class);
        verify(roomRepository).save(captor.capture());
        Room saved = captor.getValue();
        assertEquals(3, saved.getRoomFloor());
        assertEquals("305", saved.getRoomNumber());
        assertEquals(2, saved.getRoomSize()); // parsed from "Deluxe"
    }

    @Test
    void updateRoom_WhenRoomNotFound_ShouldThrow() {
        when(roomRepository.findById(1L)).thenReturn(Optional.empty());

        RoomUpdateDto dto = RoomUpdateDto.builder()
                .roomFloor(2)
                .roomNumber("202")
                .build();

        assertThrows(RuntimeException.class,
                () -> roomService.updateRoom(1L, dto));
    }

    // ============================================================
    // ✅ createRoom()
    // ============================================================
    @Test
    void createRoom_Success_ShouldSaveWithParsedSize() {
        RoomUpdateDto dto = RoomUpdateDto.builder()
                .roomFloor(4)
                .roomNumber("401")
                .roomSize("1") // => 1: Superior
                .build();

        Room saved = Room.builder()
                .id(10L)
                .roomNumber("401")
                .roomFloor(4)
                .roomSize(1)
                .build();
        when(roomRepository.save(any(Room.class))).thenReturn(saved);

        Room result = roomService.createRoom(dto);

        assertEquals(10L, result.getId());
        assertEquals("401", result.getRoomNumber());
        assertEquals(4, result.getRoomFloor());
        assertEquals(1, result.getRoomSize());
    }

    @Test
    void createRoom_WhenMissingRequiredFields_ShouldThrow() {
        RoomUpdateDto dto = RoomUpdateDto.builder()
                .roomFloor(null)   // missing
                .roomNumber("101")
                .build();

        assertThrows(RuntimeException.class,
                () -> roomService.createRoom(dto));

        RoomUpdateDto dto2 = RoomUpdateDto.builder()
                .roomFloor(1)
                .roomNumber(null)
                .build();

        assertThrows(RuntimeException.class,
                () -> roomService.createRoom(dto2));
    }

    // ============================================================
    // ✅ deleteRoom()
    // ============================================================
    @Test
    void deleteRoom_Success_ShouldDeleteEventsRequestsAssetsAndRoom() {
        Long roomId = 1L;

        Room room = Room.builder()
                .id(roomId)
                .roomNumber("101")
                .roomFloor(1)
                .roomSize(0)
                .build();
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));

        // มี maintenance request
        RequestDto req = new RequestDto(
                1L, "Leak", LocalDateTime.now(), null
        );
        when(maintainRepository.findRequestsByRoomId(roomId))
                .thenReturn(List.of(req));

        // มี roomAssets
        Asset a1 = Asset.builder().id(10L).assetName("TV").status("in_use").build();
        Asset a2 = Asset.builder().id(11L).assetName("Bed").status("in_use").build();
        RoomAsset ra1 = RoomAsset.builder().id(100L).room(room).asset(a1).build();
        RoomAsset ra2 = RoomAsset.builder().id(101L).room(room).asset(a2).build();
        when(roomAssetRepository.findByRoomId(roomId))
                .thenReturn(List.of(ra1, ra2));

        roomService.deleteRoom(roomId);

        // ลบ event
        verify(assetEventRepository).deleteByRoom_Id(roomId);
        // ลบ maintenance request
        verify(maintainRepository).deleteAllByRoomId(roomId);
        // set asset available & save
        ArgumentCaptor<Asset> assetCaptor = ArgumentCaptor.forClass(Asset.class);
        verify(assetRepository, times(2)).save(assetCaptor.capture());
        assetCaptor.getAllValues().forEach(a ->
                assertEquals("available", a.getStatus())
        );
        // ลบ room_asset
        verify(roomAssetRepository).deleteAll(List.of(ra1, ra2));
        // ลบ room
        verify(roomRepository).delete(room);
    }

    @Test
    void deleteRoom_WhenNoRequestsAndNoAssets_ShouldStillDeleteRoom() {
        Long roomId = 1L;

        Room room = Room.builder()
                .id(roomId)
                .roomNumber("101")
                .roomFloor(1)
                .roomSize(0)
                .build();
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));

        when(maintainRepository.findRequestsByRoomId(roomId))
                .thenReturn(Collections.emptyList());
        when(roomAssetRepository.findByRoomId(roomId))
                .thenReturn(Collections.emptyList());

        roomService.deleteRoom(roomId);

        verify(assetEventRepository).deleteByRoom_Id(roomId);
        verify(maintainRepository, never()).deleteAllByRoomId(anyLong());
        verify(roomAssetRepository, never()).deleteAll(anyList());
        verify(roomRepository).delete(room);
    }

    @Test
    void deleteRoom_WhenRoomNotFound_ShouldThrow() {
        when(roomRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> roomService.deleteRoom(1L));

        verify(assetEventRepository, never()).deleteByRoom_Id(anyLong());
    }
}
