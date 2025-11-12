//package com.organicnow.backend.unit.service;
//
//import com.organicnow.backend.dto.RequestDto;
//import com.organicnow.backend.repository.MaintainRepository;
//import com.organicnow.backend.service.MaintainRoomService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//class MaintainRoomServiceTest {
//
//    @Mock
//    private MaintainRepository maintainRepository;
//
//    @InjectMocks
//    private MaintainRoomService maintainRoomService;
//
//    private RequestDto sampleRequest;
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
//    }
//
//    // ✅ ทดสอบ getRequestsByRoomId()
//    @Test
//    void testGetRequestsByRoomId() {
//        when(maintainRepository.findRequestsByRoomId(101L)).thenReturn(List.of(sampleRequest));
//
//        List<RequestDto> result = maintainRoomService.getRequestsByRoomId(101L);
//
//        assertNotNull(result);
//        assertEquals(1, result.size());
//        verify(maintainRepository, times(1)).findRequestsByRoomId(101L);
//    }
//
//    // ✅ ทดสอบเมื่อไม่มี request ในห้อง
//    @Test
//    void testGetRequestsByRoomId_NoRequests() {
//        when(maintainRepository.findRequestsByRoomId(202L)).thenReturn(List.of());
//
//        List<RequestDto> result = maintainRoomService.getRequestsByRoomId(202L);
//
//        assertNotNull(result);
//        assertTrue(result.isEmpty());
//        verify(maintainRepository, times(1)).findRequestsByRoomId(202L);
//    }
//}
