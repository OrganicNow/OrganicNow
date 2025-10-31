//package com.organicnow.backend.controller;
//
//import com.organicnow.backend.dto.*;
//import com.organicnow.backend.service.MaintainRoomService;
//import com.organicnow.backend.service.MaintainService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.http.ResponseEntity;
//
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//class MaintainControllerTest {
//
//    @Mock
//    private MaintainRoomService maintainRoomService;
//
//    @Mock
//    private MaintainService maintainService;
//
//    @InjectMocks
//    private MaintainController maintainController;
//
//    private RequestDto sampleRequest;
//    private MaintainDto sampleMaintain;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//
//        // ✅ mock RequestDto
//        sampleRequest = new RequestDto();
//        try {
//            sampleRequest.getClass().getMethod("setRequestId", Long.class).invoke(sampleRequest, 1L);
//        } catch (Exception ignored) {}
//        try {
//            sampleRequest.getClass().getMethod("setRequestDetail", String.class).invoke(sampleRequest, "Broken light");
//        } catch (Exception ignored) {}
//
//        // ✅ mock MaintainDto
//        sampleMaintain = new MaintainDto();
//        try {
//            sampleMaintain.getClass().getMethod("setMaintainId", Long.class).invoke(sampleMaintain, 1L);
//        } catch (Exception ignored) {}
//        try {
//            sampleMaintain.getClass().getMethod("setDescription", String.class).invoke(sampleMaintain, "Fix air conditioner");
//        } catch (Exception ignored) {}
//    }
//
//    // ✅ GET /maintain/{roomId}/requests
//    @Test
//    void testGetRequestsByRoom() {
//        when(maintainRoomService.getRequestsByRoomId(101L)).thenReturn(List.of(sampleRequest));
//
//        ApiResponse<List<RequestDto>> response = maintainController.getRequestsByRoom(101L);
//
//        // ✅ ใช้ getResult() แทน getData()
//        assertNotNull(response);
//        assertEquals("success", response.getStatus());
//        assertEquals(1, response.getResult().size());
//        verify(maintainRoomService, times(1)).getRequestsByRoomId(101L);
//    }
//
//    // ✅ GET /maintain/list
//    @Test
//    void testList() {
//        when(maintainService.getAll()).thenReturn(List.of(sampleMaintain));
//
//        ResponseEntity<List<MaintainDto>> response = maintainController.list();
//
//        assertEquals(200, response.getStatusCode().value());
//        assertNotNull(response.getBody());
//        assertEquals(1, response.getBody().size());
//        verify(maintainService, times(1)).getAll();
//    }
//
//    // ✅ GET /maintain/{id} - found
//    @Test
//    void testGetByIdFound() {
//        when(maintainService.getById(1L)).thenReturn(Optional.of(sampleMaintain));
//
//        ResponseEntity<MaintainDto> response = maintainController.get(1L);
//
//        assertEquals(200, response.getStatusCode().value());
//        assertNotNull(response.getBody());
//        verify(maintainService, times(1)).getById(1L);
//    }
//
//    // ✅ GET /maintain/{id} - not found
//    @Test
//    void testGetByIdNotFound() {
//        when(maintainService.getById(99L)).thenReturn(Optional.empty());
//
//        ResponseEntity<MaintainDto> response = maintainController.get(99L);
//
//        assertEquals(404, response.getStatusCode().value());
//        verify(maintainService, times(1)).getById(99L);
//    }
//
//    // ✅ POST /maintain/create
//    @Test
//    void testCreate() {
//        CreateMaintainRequest req = new CreateMaintainRequest();
//        when(maintainService.create(req)).thenReturn(sampleMaintain);
//
//        ResponseEntity<?> response = maintainController.create(req);
//
//        assertEquals(200, response.getStatusCode().value());
//        assertTrue(response.getBody() instanceof MaintainDto);
//        verify(maintainService, times(1)).create(req);
//    }
//
//    // ✅ PUT /maintain/update/{id}
//    @Test
//    void testUpdate() {
//        UpdateMaintainRequest req = new UpdateMaintainRequest();
//        when(maintainService.update(1L, req)).thenReturn(sampleMaintain);
//
//        ResponseEntity<?> response = maintainController.update(1L, req);
//
//        assertEquals(200, response.getStatusCode().value());
//        assertTrue(response.getBody() instanceof MaintainDto);
//        verify(maintainService, times(1)).update(1L, req);
//    }
//
//    // ✅ DELETE /maintain/{id}
//    @Test
//    void testDelete() {
//        doNothing().when(maintainService).delete(1L);
//
//        ResponseEntity<?> response = maintainController.delete(1L);
//
//        assertEquals(200, response.getStatusCode().value());
//        assertNull(response.getBody());
//        verify(maintainService, times(1)).delete(1L);
//    }
//
//    // ✅ POST /maintain/create → throw Exception
//    @Test
//    void testCreate_Exception() {
//        CreateMaintainRequest req = new CreateMaintainRequest();
//        when(maintainService.create(req)).thenThrow(new RuntimeException("DB Error"));
//
//        ResponseEntity<?> response = maintainController.create(req);
//
//        assertEquals(400, response.getStatusCode().value());
//        assertTrue(response.getBody().toString().contains("Create failed"));
//    }
//
//    // ✅ PUT /maintain/update/{id} → throw Exception
//    @Test
//    void testUpdate_Exception() {
//        UpdateMaintainRequest req = new UpdateMaintainRequest();
//        when(maintainService.update(1L, req)).thenThrow(new RuntimeException("Update Error"));
//
//        ResponseEntity<?> response = maintainController.update(1L, req);
//
//        assertEquals(400, response.getStatusCode().value());
//        assertTrue(response.getBody().toString().contains("Update failed"));
//    }
//
//    // ✅ DELETE /maintain/{id} → throw Exception
//    @Test
//    void testDelete_Exception() {
//        doThrow(new RuntimeException("Cannot delete")).when(maintainService).delete(1L);
//
//        ResponseEntity<?> response = maintainController.delete(1L);
//
//        assertEquals(400, response.getStatusCode().value());
//        assertTrue(response.getBody().toString().contains("Delete failed"));
//    }
//}
