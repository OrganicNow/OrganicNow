package com.organicnow.backend.integration.controller;

import com.organicnow.backend.controller.RoomController;
import com.organicnow.backend.dto.AssetEventRequestDto;
import com.organicnow.backend.dto.RoomDetailDto;
import com.organicnow.backend.dto.RoomUpdateDto;
import com.organicnow.backend.model.AssetEvent;
import com.organicnow.backend.model.Room;
import com.organicnow.backend.service.RoomService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.when;
import java.util.List;


import java.util.List;
import java.util.Arrays; // 👈 เพิ่ม import นี้ด้านบน


import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.isEmptyString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = RoomController.class)
@AutoConfigureMockMvc(addFilters = false)
class RoomControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    RoomService roomService;

    // -------------------------------------------------------
    // 1) GET /room/{id}/detail
    // -------------------------------------------------------
    @Test
    void getRoomDetail_whenFound_shouldReturn200() throws Exception {
        RoomDetailDto dto = Mockito.mock(RoomDetailDto.class);
        when(roomService.getRoomDetail(1L)).thenReturn(dto);

        mockMvc.perform(get("/room/{id}/detail", 1L))
                .andExpect(status().isOk())
                .andExpect(content().string(not(isEmptyString())));

        verify(roomService).getRoomDetail(1L);
    }

    @Test
    void getRoomDetail_whenNotFound_shouldReturn404() throws Exception {
        when(roomService.getRoomDetail(99L)).thenReturn(null);

        mockMvc.perform(get("/room/{id}/detail", 99L))
                .andExpect(status().isNotFound());

        verify(roomService).getRoomDetail(99L);
    }

    // -------------------------------------------------------
    // 2) GET /room
    // -------------------------------------------------------
    @Test
    void getAllRooms_whenNonEmpty_shouldReturn200AndList() throws Exception {
        RoomDetailDto r1 = Mockito.mock(RoomDetailDto.class);
        RoomDetailDto r2 = Mockito.mock(RoomDetailDto.class);

        when(roomService.getAllRooms()).thenReturn(List.of(r1, r2));

        mockMvc.perform(get("/room"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(roomService).getAllRooms();
    }

    @Test
    void getAllRooms_whenEmpty_shouldReturn204() throws Exception {
        when(roomService.getAllRooms()).thenReturn(List.of());

        mockMvc.perform(get("/room"))
                .andExpect(status().isNoContent());

        verify(roomService).getAllRooms();
    }

    // -------------------------------------------------------
    // 3) GET /room/list (ใช้ logic เดียวกับ getAllRooms)
    // -------------------------------------------------------
    @Test
    void getAllRoomsList_shouldDelegateToGetAllRooms_andReturn200() throws Exception {
        RoomDetailDto r1 = Mockito.mock(RoomDetailDto.class);

        when(roomService.getAllRooms()).thenReturn(List.of(r1));

        mockMvc.perform(get("/room/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(roomService).getAllRooms();
    }

    // -------------------------------------------------------
    // 4) POST /room - createRoom
    // -------------------------------------------------------
    @Test
    void createRoom_withValidBody_shouldReturn200() throws Exception {
        Room newRoom = new Room();
        when(roomService.createRoom(any(RoomUpdateDto.class))).thenReturn(newRoom);

        String json = """
                {
                  "roomNumber": "101",
                  "roomFloor": 1,
                  "roomSize": 30
                }
                """;

        mockMvc.perform(
                        post("/room")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isOk());

        verify(roomService).createRoom(any(RoomUpdateDto.class));
    }

    @Test
    void createRoom_whenServiceThrows_shouldReturn400() throws Exception {
        when(roomService.createRoom(any(RoomUpdateDto.class)))
                .thenThrow(new RuntimeException("cannot create"));

        String json = """
                {
                  "roomNumber": "101",
                  "roomFloor": 1,
                  "roomSize": 30
                }
                """;

        mockMvc.perform(
                        post("/room")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().string("cannot create"));
    }

    // -------------------------------------------------------
    // 5) POST /room/{roomId}/assets/{assetId} - addAssetToRoom
    // -------------------------------------------------------
    @Test
    void addAssetToRoom_shouldReturn200AndMessage() throws Exception {
        mockMvc.perform(
                        post("/room/{roomId}/assets/{assetId}", 1L, 10L)
                )
                .andExpect(status().isOk())
                .andExpect(content().string("Asset added successfully"));

        verify(roomService).addAssetToRoom(1L, 10L);
    }

    @Test
    void addAssetToRoom_whenServiceThrows_shouldReturn400() throws Exception {
        doThrow(new RuntimeException("asset exists"))
                .when(roomService).addAssetToRoom(1L, 10L);

        mockMvc.perform(
                        post("/room/{roomId}/assets/{assetId}", 1L, 10L)
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().string("asset exists"));
    }

    // -------------------------------------------------------
    // 6) DELETE /room/{roomId}/assets/{assetId} - removeAssetFromRoom
    // -------------------------------------------------------
    @Test
    void removeAssetFromRoom_shouldReturn200AndMessage() throws Exception {
        mockMvc.perform(
                        delete("/room/{roomId}/assets/{assetId}", 2L, 20L)
                )
                .andExpect(status().isOk())
                .andExpect(content().string("Asset removed successfully"));

        verify(roomService).removeAssetFromRoom(2L, 20L);
    }

    @Test
    void removeAssetFromRoom_whenServiceThrows_shouldReturn400() throws Exception {
        doThrow(new RuntimeException("not found"))
                .when(roomService).removeAssetFromRoom(2L, 20L);

        mockMvc.perform(
                        delete("/room/{roomId}/assets/{assetId}", 2L, 20L)
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().string("not found"));
    }

    // -------------------------------------------------------
    // 7) PUT /room/{roomId}/assets - updateRoomAssets
    // -------------------------------------------------------
    @Test
    void updateRoomAssets_shouldReturn200AndMessage() throws Exception {
        String json = "[1,2,3]";

        mockMvc.perform(
                        put("/room/{roomId}/assets", 3L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isOk())
                .andExpect(content().string("Room assets updated successfully"));

        verify(roomService).updateRoomAssets(eq(3L), anyList());
    }

    @Test
    void updateRoomAssets_whenServiceThrows_shouldReturn400() throws Exception {
        doThrow(new RuntimeException("update fail"))
                .when(roomService).updateRoomAssets(eq(3L), anyList());

        String json = "[1,2,3]";

        mockMvc.perform(
                        put("/room/{roomId}/assets", 3L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().string("update fail"));
    }

    // -------------------------------------------------------
    // 8) PUT /room/{id} - updateRoomInfo
    // -------------------------------------------------------
    @Test
    void updateRoomInfo_shouldReturn200AndMessage() throws Exception {
        String json = """
                {
                  "roomNumber": "102",
                  "roomFloor": 2,
                  "roomSize": 25
                }
                """;

        mockMvc.perform(
                        put("/room/{id}", 4L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isOk())
                .andExpect(content().string("Room info updated successfully"));

        verify(roomService).updateRoom(eq(4L), any(RoomUpdateDto.class));
    }

    @Test
    void updateRoomInfo_whenServiceThrows_shouldReturn400() throws Exception {
        doThrow(new RuntimeException("update error"))
                .when(roomService).updateRoom(eq(4L), any(RoomUpdateDto.class));

        String json = """
                {
                  "roomNumber": "102",
                  "roomFloor": 2,
                  "roomSize": 25
                }
                """;

        mockMvc.perform(
                        put("/room/{id}", 4L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().string("update error"));
    }

    // -------------------------------------------------------
    // 9) DELETE /room/{id} - deleteRoom
    // -------------------------------------------------------
    @Test
    void deleteRoom_shouldReturn200AndMessage() throws Exception {
        mockMvc.perform(delete("/room/{id}", 5L))
                .andExpect(status().isOk())
                .andExpect(content().string("Room deleted successfully"));

        verify(roomService).deleteRoom(5L);
    }

    @Test
    void deleteRoom_whenRuntimeException_shouldReturn400() throws Exception {
        doThrow(new RuntimeException("room in use"))
                .when(roomService).deleteRoom(6L);

        mockMvc.perform(delete("/room/{id}", 6L))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Failed to delete room: room in use"));
    }

    // (Note: branch catch(Exception e) ใช้ checked exception จริง ๆ ยาก เพราะ method ไม่ได้ประกาศ throws
    //  เลยโฟกัสเทส branch RuntimeException ที่ใช้จริงใน service)

    // -------------------------------------------------------
    // 10) PUT /room/{roomId}/assets/event - updateRoomAssetsWithReason
    // -------------------------------------------------------
    @Test
    void updateRoomAssetsWithReason_shouldReturn200AndMessage() throws Exception {
        // ไม่ส่ง reasonType เพื่อเลี่ยงปัญหา enum mapping ถ้าใน DTO เป็น enum
        String json = """
                {
                  "assetIds": [1,2,3],
                  "note": "move assets"
                }
                """;

        mockMvc.perform(
                        put("/room/{roomId}/assets/event", 7L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isOk())
                .andExpect(content().string("Room assets updated with reason logged"));

        verify(roomService).updateRoomAssetsWithReason(eq(7L), anyList(), any(), eq("move assets"));
    }

    @Test
    void updateRoomAssetsWithReason_whenServiceThrows_shouldReturn400() throws Exception {
        doThrow(new RuntimeException("reason fail"))
                .when(roomService).updateRoomAssetsWithReason(eq(7L), anyList(), any(), anyString());

        String json = """
                {
                  "assetIds": [1,2,3],
                  "note": "move assets"
                }
                """;

        mockMvc.perform(
                        put("/room/{roomId}/assets/event", 7L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().string("reason fail"));
    }

    // -------------------------------------------------------
    // 11) GET /room/{roomId}/events - getRoomAssetEvents
    // -------------------------------------------------------
    @Test
    void getRoomAssetEvents_shouldReturnList() throws Exception {
        Long roomId = 8L;

        AssetEvent e1 = new AssetEvent();
        AssetEvent e2 = new AssetEvent();

        when(roomService.getRoomAssetEvents(roomId))
                .thenReturn((java.util.List) java.util.List.of(e1, e2));  // 👈 ใส่ cast แบบนี้

        mockMvc.perform(get("/room/{roomId}/events", roomId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }



    @Test
    void getRoomAssetEvents_whenServiceThrows_shouldReturn500() throws Exception {
        when(roomService.getRoomAssetEvents(9L))
                .thenThrow(new RuntimeException("events fail"));

        mockMvc.perform(get("/room/{roomId}/events", 9L))
                .andExpect(status().isInternalServerError());
    }
}
