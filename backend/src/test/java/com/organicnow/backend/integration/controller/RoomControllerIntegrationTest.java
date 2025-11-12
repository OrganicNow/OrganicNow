//package com.organicnow.backend.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.organicnow.backend.model.*;
//import com.organicnow.backend.repository.*;
//import com.organicnow.backend.service.RoomService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.test.annotation.DirtiesContext;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//@Transactional
//@ActiveProfiles("test") // ✅ ใช้ application-test.yml
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
//class RoomControllerIntegrationTest {
//
//    @Autowired private MockMvc mockMvc;
//    @Autowired private ObjectMapper objectMapper;
//
//    @Autowired private RoomRepository roomRepository;
//    @Autowired private AssetRepository assetRepository;
//    @Autowired private RoomAssetRepository roomAssetRepository;
//    @Autowired private AssetGroupRepository assetGroupRepository;
//
//    private Room testRoom;
//    private Asset testAsset;
//    private AssetGroup testGroup;
//
//    @BeforeEach
//    void setup() {
//        // ✅ เตรียมข้อมูลจริงใน DB (persist)
//        testGroup = AssetGroup.builder()
//                .assetGroupName("Furniture")
//                .build();
//        assetGroupRepository.save(testGroup);
//
//        testRoom = Room.builder()
//                .roomFloor(2)
//                .roomNumber("B201")
//                .build();
//        roomRepository.save(testRoom);
//
//        testAsset = Asset.builder()
//                .assetName("Bed")
//                .status("available")
//                .assetGroup(testGroup)
//                .build();
//        assetRepository.save(testAsset);
//    }
//
//    // ===========================
//    // 🧩 ROOM CONTROLLER TESTS
//    // ===========================
//
//    @Test
//    @DisplayName("GET /room - should return 200 OK when rooms exist")
//    void testGetAllRooms_ShouldReturnOk() throws Exception {
//        mockMvc.perform(get("/room"))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
//
//        List<Room> rooms = roomRepository.findAll();
//        assertThat(rooms).isNotEmpty();
//    }
//
//    @Test
//    @DisplayName("GET /room/{id}/detail - should return room detail or 404 if not found")
//    void testGetRoomDetail_ShouldReturnOkOrNotFound() throws Exception {
//        mockMvc.perform(get("/room/{id}/detail", testRoom.getId()))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
//    }
//
//    @Test
//    @DisplayName("POST /room/{roomId}/assets/{assetId} - should add asset to room")
//    void testAddAssetToRoom_ShouldReturnOk() throws Exception {
//        mockMvc.perform(post("/room/{roomId}/assets/{assetId}",
//                        testRoom.getId(), testAsset.getId()))
//                .andExpect(status().isOk())
//                .andExpect(content().string("Asset added successfully"));
//
//        boolean exists = roomAssetRepository.existsByRoomIdAndAssetId(testRoom.getId(), testAsset.getId());
//        assertThat(exists).isTrue();
//    }
//
//    @Test
//    @DisplayName("DELETE /room/{roomId}/assets/{assetId} - should remove asset from room")
//    void testRemoveAssetFromRoom_ShouldReturnOk() throws Exception {
//        // ✅ เตรียมความสัมพันธ์ก่อน
//        RoomAsset roomAsset = RoomAsset.builder()
//                .room(testRoom)
//                .asset(testAsset)
//                .build();
//        roomAssetRepository.save(roomAsset);
//
//        mockMvc.perform(delete("/room/{roomId}/assets/{assetId}",
//                        testRoom.getId(), testAsset.getId()))
//                .andExpect(status().isOk())
//                .andExpect(content().string("Asset removed successfully"));
//
//        boolean exists = roomAssetRepository.existsByRoomIdAndAssetId(testRoom.getId(), testAsset.getId());
//        assertThat(exists).isFalse();
//    }
//
//    @Test
//    @DisplayName("PUT /room/{roomId}/assets - should update room assets successfully")
//    void testUpdateRoomAssets_ShouldReturnOk() throws Exception {
//        String json = objectMapper.writeValueAsString(List.of(testAsset.getId()));
//
//        mockMvc.perform(put("/room/{roomId}/assets", testRoom.getId())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(json))
//                .andExpect(status().isOk())
//                .andExpect(content().string("Room assets updated successfully"));
//    }
//
//    // ===========================
//    // 🧩 ROOM SERVICE INTEGRATION TEST
//    // ===========================
//
//    @Nested
//    @DisplayName("RoomService Integration Tests")
//    class RoomServiceIntegrationTest {
//
//        @Autowired private RoomService roomService;
//        @Autowired private RoomRepository roomRepository;
//        @Autowired private AssetRepository assetRepository;
//        @Autowired private RoomAssetRepository roomAssetRepository;
//        @Autowired private AssetGroupRepository assetGroupRepository;
//
//        @Test
//        @DisplayName("addAssetToRoom() - should persist relation correctly")
//        void testAddAssetToRoom_ShouldPersistRelation() {
//            AssetGroup group = assetGroupRepository.save(AssetGroup.builder()
//                    .assetGroupName("Electronics")
//                    .build());
//
//            Room room = roomRepository.save(Room.builder()
//                    .roomFloor(3)
//                    .roomNumber("C301")
//                    .build());
//
//            Asset asset = assetRepository.save(Asset.builder()
//                    .assetName("Chair")
//                    .status("available")
//                    .assetGroup(group)
//                    .build());
//
//            roomService.addAssetToRoom(room.getId(), asset.getId());
//
//            boolean exists = roomAssetRepository.existsByRoomIdAndAssetId(room.getId(), asset.getId());
//            assertThat(exists).isTrue();
//        }
//
//        @Test
//        @DisplayName("removeAssetFromRoom() - should remove relation correctly")
//        void testRemoveAssetFromRoom_ShouldRemoveRelation() {
//            AssetGroup group = assetGroupRepository.save(AssetGroup.builder()
//                    .assetGroupName("Appliances")
//                    .build());
//
//            Room room = roomRepository.save(Room.builder()
//                    .roomFloor(4)
//                    .roomNumber("D401")
//                    .build());
//
//            Asset asset = assetRepository.save(Asset.builder()
//                    .assetName("Fan")
//                    .status("available")
//                    .assetGroup(group)
//                    .build());
//
//            roomService.addAssetToRoom(room.getId(), asset.getId());
//            assertThat(roomAssetRepository.existsByRoomIdAndAssetId(room.getId(), asset.getId())).isTrue();
//
//            roomService.removeAssetFromRoom(room.getId(), asset.getId());
//
//            boolean exists = roomAssetRepository.existsByRoomIdAndAssetId(room.getId(), asset.getId());
//            assertThat(exists).isFalse();
//        }
//
//        @Test
//        @DisplayName("updateRoomAssets() - should replace assets correctly")
//        void testUpdateRoomAssets_ShouldReplaceAssets() {
//            AssetGroup group = assetGroupRepository.save(AssetGroup.builder()
//                    .assetGroupName("OfficeItems")
//                    .build());
//
//            Room room = roomRepository.save(Room.builder()
//                    .roomFloor(5)
//                    .roomNumber("E501")
//                    .build());
//
//            Asset a1 = assetRepository.save(Asset.builder()
//                    .assetName("TV")
//                    .status("available")
//                    .assetGroup(group)
//                    .build());
//
//            Asset a2 = assetRepository.save(Asset.builder()
//                    .assetName("Desk")
//                    .status("available")
//                    .assetGroup(group)
//                    .build());
//
//            roomService.addAssetToRoom(room.getId(), a1.getId());
//
//            roomService.updateRoomAssets(room.getId(), List.of(a2.getId()));
//
//            boolean hasA1 = roomAssetRepository.existsByRoomIdAndAssetId(room.getId(), a1.getId());
//            boolean hasA2 = roomAssetRepository.existsByRoomIdAndAssetId(room.getId(), a2.getId());
//
//            assertThat(hasA1).isFalse();
//            assertThat(hasA2).isTrue();
//        }
//    }
//}
