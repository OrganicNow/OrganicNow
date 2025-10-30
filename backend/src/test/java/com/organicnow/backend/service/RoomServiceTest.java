//package com.organicnow.backend.service;
//
//import com.organicnow.backend.dto.*;
//import com.organicnow.backend.model.*;
//import com.organicnow.backend.repository.*;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.*;
//
//import java.util.*;
//
//import static org.assertj.core.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//class RoomServiceTest {
//
//    @Mock private RoomRepository roomRepository;
//    @Mock private AssetRepository assetRepository;
//    @Mock private MaintainRepository maintainRepository;
//    @Mock private RoomAssetRepository roomAssetRepository;
//
//    @InjectMocks private RoomService roomService;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    // ✅ 1. getAllRooms() — มีข้อมูลครบ
//    @Test
//    void getAllRooms_shouldReturnRoomsWithAssetsAndRequests() {
//        RoomDetailDto room1 = new RoomDetailDto();
//        room1.setRoomId(1L);
//        room1.setRoomNumber("A101");
//
//        when(roomRepository.findAllRooms()).thenReturn(List.of(room1));
//
//        // mock asset rows
//        List<Object[]> rows = new ArrayList<>();
//        rows.add(new Object[]{1L, 10L, "Bed", "Furniture", 1, "A101"});
//        when(roomAssetRepository.findAssetsByRoomIds(anyList())).thenReturn(rows);
//
//        // mock requests
//        RequestDto req = new RequestDto();
//        req.setId(100L);
//        when(maintainRepository.findRequestsByRoomId(1L)).thenReturn(List.of(req));
//
//        List<RoomDetailDto> result = roomService.getAllRooms();
//
//        assertThat(result).hasSize(1);
//        assertThat(result.get(0).getAssets()).hasSize(1);
//        assertThat(result.get(0).getRequests()).hasSize(1);
//        assertThat(result.get(0).getAssets().get(0).getAssetName()).isEqualTo("Bed");
//    }
//
//    // ✅ 2. getAllRooms() — ไม่มีห้อง
//    @Test
//    void getAllRooms_shouldReturnEmptyListIfNoRooms() {
//        when(roomRepository.findAllRooms()).thenReturn(Collections.emptyList());
//        List<RoomDetailDto> result = roomService.getAllRooms();
//        assertThat(result).isEmpty();
//        verifyNoInteractions(roomAssetRepository, maintainRepository);
//    }
//
//    // ✅ 3. getRoomDetail() — มีข้อมูลครบ
//    @Test
//    void getRoomDetail_shouldReturnFullData() {
//        RoomDetailDto dto = new RoomDetailDto();
//        dto.setRoomId(1L);
//        when(roomRepository.findRoomDetail(1L)).thenReturn(dto);
//
//        AssetDto asset = new AssetDto(10L, "Chair", "Furniture", 1, "A101");
//        when(assetRepository.findAssetsByRoomId(1L)).thenReturn(List.of(asset));
//
//        RequestDto req = new RequestDto();
//        req.setId(100L);
//        when(maintainRepository.findRequestsByRoomId(1L)).thenReturn(List.of(req));
//
//        RoomDetailDto result = roomService.getRoomDetail(1L);
//
//        assertThat(result.getAssets()).hasSize(1);
//        assertThat(result.getRequests()).hasSize(1);
//    }
//
//    // ✅ 4. getRoomDetail() — ไม่มีห้อง
//    @Test
//    void getRoomDetail_shouldReturnNullIfRoomNotFound() {
//        when(roomRepository.findRoomDetail(999L)).thenReturn(null);
//        RoomDetailDto result = roomService.getRoomDetail(999L);
//        assertThat(result).isNull();
//    }
//
//    // ✅ 5. addAssetToRoom() — เพิ่มสำเร็จ
//    @Test
//    void addAssetToRoom_shouldAddAssetSuccessfully() {
//        Long roomId = 1L, assetId = 10L;
//        when(roomAssetRepository.existsByRoomIdAndAssetId(roomId, assetId)).thenReturn(false);
//
//        Room room = new Room(); room.setId(roomId);
//        Asset asset = new Asset(); asset.setId(assetId); asset.setStatus("available");
//
//        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
//        when(assetRepository.findAvailableById(assetId)).thenReturn(asset);
//
//        roomService.addAssetToRoom(roomId, assetId);
//
//        verify(roomAssetRepository).save(any(RoomAsset.class));
//        verify(assetRepository).save(asset);
//        assertThat(asset.getStatus()).isEqualTo("in_use");
//    }
//
//    // ❌ 6. addAssetToRoom() — ซ้ำ
//    @Test
//    void addAssetToRoom_shouldThrowWhenDuplicate() {
//        when(roomAssetRepository.existsByRoomIdAndAssetId(1L, 2L)).thenReturn(true);
//        assertThatThrownBy(() -> roomService.addAssetToRoom(1L, 2L))
//                .isInstanceOf(RuntimeException.class)
//                .hasMessage("Asset already exists in this room");
//    }
//
//    // ❌ 7. addAssetToRoom() — ห้องไม่เจอ
//    @Test
//    void addAssetToRoom_shouldThrowIfRoomNotFound() {
//        when(roomAssetRepository.existsByRoomIdAndAssetId(1L, 2L)).thenReturn(false);
//        when(roomRepository.findById(1L)).thenReturn(Optional.empty());
//        assertThatThrownBy(() -> roomService.addAssetToRoom(1L, 2L))
//                .isInstanceOf(RuntimeException.class)
//                .hasMessage("Room not found");
//    }
//
//    // ❌ 8. addAssetToRoom() — asset ไม่ว่าง
//    @Test
//    void addAssetToRoom_shouldThrowIfAssetUnavailable() {
//        when(roomAssetRepository.existsByRoomIdAndAssetId(1L, 2L)).thenReturn(false);
//        Room room = new Room(); room.setId(1L);
//        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
//        when(assetRepository.findAvailableById(2L)).thenReturn(null);
//        assertThatThrownBy(() -> roomService.addAssetToRoom(1L, 2L))
//                .isInstanceOf(RuntimeException.class)
//                .hasMessage("Asset not available or not found");
//    }
//
//    // ✅ 9. removeAssetFromRoom() — เอาออกได้
//    @Test
//    void removeAssetFromRoom_shouldWorkCorrectly() {
//        Long roomId = 1L, assetId = 10L;
//        Asset asset = new Asset(); asset.setId(assetId); asset.setStatus("in_use");
//        RoomAsset ra = new RoomAsset(); ra.setAsset(asset);
//
//        when(roomAssetRepository.findByRoomIdAndAssetId(roomId, assetId))
//                .thenReturn(Optional.of(ra));
//        when(assetRepository.findById(assetId))
//                .thenReturn(Optional.of(asset));
//
//        roomService.removeAssetFromRoom(roomId, assetId);
//
//        verify(roomAssetRepository).delete(ra);
//        verify(assetRepository).save(asset);
//        assertThat(asset.getStatus()).isEqualTo("available");
//    }
//
//    // ❌ 10. removeAssetFromRoom() — ไม่เจอ asset
//    @Test
//    void removeAssetFromRoom_shouldThrowIfNotFound() {
//        when(roomAssetRepository.findByRoomIdAndAssetId(1L, 99L))
//                .thenReturn(Optional.empty());
//        assertThatThrownBy(() -> roomService.removeAssetFromRoom(1L, 99L))
//                .isInstanceOf(RuntimeException.class)
//                .hasMessage("Asset not found in this room");
//    }
//
//    // ✅ 11. updateRoomAssets() — เพิ่มและลบ asset ถูกต้อง
//    @Test
//    void updateRoomAssets_shouldAddAndRemoveCorrectly() {
//        Long roomId = 1L;
//
//        Room room = new Room(); room.setId(roomId);
//
//        Asset oldAsset = new Asset(); oldAsset.setId(10L); oldAsset.setStatus("in_use");
//        RoomAsset ra = new RoomAsset(); ra.setAsset(oldAsset); ra.setRoom(room);
//
//        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
//        when(roomAssetRepository.findByRoomId(roomId)).thenReturn(List.of(ra));
//
//        Asset newAsset = new Asset(); newAsset.setId(20L);
//        when(assetRepository.findAllById(Set.of(20L))).thenReturn(List.of(newAsset));
//
//        roomService.updateRoomAssets(roomId, List.of(20L));
//
//        verify(roomAssetRepository).delete(ra);
//        verify(roomAssetRepository).save(any(RoomAsset.class));
//        verify(assetRepository, atLeastOnce()).save(any(Asset.class));
//        assertThat(newAsset.getStatus()).isEqualTo("in_use");
//    }
//
//    // ❌ 12. updateRoomAssets() — ไม่เจอห้อง
//    @Test
//    void updateRoomAssets_shouldThrowIfRoomNotFound() {
//        when(roomRepository.findById(999L)).thenReturn(Optional.empty());
//        assertThatThrownBy(() -> roomService.updateRoomAssets(999L, List.of(1L)))
//                .isInstanceOf(RuntimeException.class)
//                .hasMessage("Room not found");
//    }
//
//    // ✅ 13. updateRoom() — อัปเดตห้องได้
//    @Test
//    void updateRoom_shouldUpdateSuccessfully() {
//        Room room = new Room();
//        room.setId(1L);
//        room.setRoomFloor(1);
//        room.setRoomNumber("A101");
//
//        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
//
//        RoomUpdateDto dto = new RoomUpdateDto();
//        dto.setRoomNumber("B202");
//        dto.setRoomFloor(2);
//
//        roomService.updateRoom(1L, dto);
//
//        verify(roomRepository).save(room);
//        assertThat(room.getRoomNumber()).isEqualTo("B202");
//        assertThat(room.getRoomFloor()).isEqualTo(2);
//    }
//
//    // ❌ 14. updateRoom() — ไม่เจอห้อง
//    @Test
//    void updateRoom_shouldThrowIfRoomNotFound() {
//        when(roomRepository.findById(1L)).thenReturn(Optional.empty());
//        RoomUpdateDto dto = new RoomUpdateDto();
//        assertThatThrownBy(() -> roomService.updateRoom(1L, dto))
//                .isInstanceOf(RuntimeException.class)
//                .hasMessage("Room not found");
//    }
//}
