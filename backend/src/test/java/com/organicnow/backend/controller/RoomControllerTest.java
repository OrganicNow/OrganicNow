//package com.organicnow.backend.controller;
//
//import com.organicnow.backend.dto.RoomDetailDto;
//import com.organicnow.backend.service.RoomService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.http.ResponseEntity;
//
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//class RoomControllerTest {
//
//    @Mock
//    private RoomService roomService;
//
//    @InjectMocks
//    private RoomController roomController;
//
//    private RoomDetailDto sampleRoom;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//
//        // ✅ mock RoomDetailDto
//        sampleRoom = new RoomDetailDto();
//        try {
//            sampleRoom.getClass().getMethod("setRoomId", Long.class).invoke(sampleRoom, 1L);
//        } catch (Exception ignored) {}
//        try {
//            sampleRoom.getClass().getMethod("setRoomNumber", String.class).invoke(sampleRoom, "101");
//        } catch (Exception ignored) {}
//        try {
//            sampleRoom.getClass().getMethod("setRoomFloor", Integer.class).invoke(sampleRoom, 1);
//        } catch (Exception ignored) {}
//    }
//
//    // ✅ GET /rooms/{id}/detail - พบข้อมูล
//    @Test
//    void testGetRoomDetail_Found() {
//        when(roomService.getRoomDetail(1L)).thenReturn(sampleRoom);
//
//        ResponseEntity<RoomDetailDto> response = roomController.getRoomDetail(1L);
//
//        assertEquals(200, response.getStatusCode().value());
//        assertNotNull(response.getBody());
//        verify(roomService, times(1)).getRoomDetail(1L);
//    }
//
//    // ✅ GET /rooms/{id}/detail - ไม่พบข้อมูล
//    @Test
//    void testGetRoomDetail_NotFound() {
//        when(roomService.getRoomDetail(99L)).thenReturn(null);
//
//        ResponseEntity<RoomDetailDto> response = roomController.getRoomDetail(99L);
//
//        assertEquals(404, response.getStatusCode().value());
//        assertNull(response.getBody());
//        verify(roomService, times(1)).getRoomDetail(99L);
//    }
//
//    // ✅ GET /rooms - มีข้อมูล
//    @Test
//    void testGetAllRooms_WithData() {
//        when(roomService.getAllRooms()).thenReturn(List.of(sampleRoom));
//
//        ResponseEntity<List<RoomDetailDto>> response = roomController.getAllRooms();
//
//        assertEquals(200, response.getStatusCode().value());
//        assertNotNull(response.getBody());
//        assertEquals(1, response.getBody().size());
//        verify(roomService, times(1)).getAllRooms();
//    }
//
//    // ✅ GET /rooms - ไม่มีข้อมูล
//    @Test
//    void testGetAllRooms_Empty() {
//        when(roomService.getAllRooms()).thenReturn(List.of());
//
//        ResponseEntity<List<RoomDetailDto>> response = roomController.getAllRooms();
//
//        assertEquals(204, response.getStatusCode().value());
//        assertNull(response.getBody());
//        verify(roomService, times(1)).getAllRooms();
//    }
//}
