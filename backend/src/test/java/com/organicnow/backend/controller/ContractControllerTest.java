//package com.organicnow.backend.controller;
//
//import com.organicnow.backend.dto.TenantDto;
//import com.organicnow.backend.service.ContractService;
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
//class ContractControllerTest {
//
//    @Mock
//    private ContractService contractService;
//
//    @InjectMocks
//    private ContractController contractController;
//
//    private TenantDto sampleTenant;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//
//        // ✅ mock TenantDto ขึ้นเอง (ใช้ reflection ป้องกันปัญหา field ไม่ตรง)
//        sampleTenant = new TenantDto();
//        try {
//            sampleTenant.getClass().getMethod("setTenantId", Long.class).invoke(sampleTenant, 1L);
//        } catch (Exception ignored) {}
//        try {
//            sampleTenant.getClass().getMethod("setTenantName", String.class).invoke(sampleTenant, "John Doe");
//        } catch (Exception ignored) {}
//    }
//
//    // ✅ ทดสอบ getTenantList()
//    @Test
//    void testGetTenantList() {
//        when(contractService.getTenantList()).thenReturn(List.of(sampleTenant));
//
//        List<TenantDto> result = contractController.getTenantList();
//
//        assertNotNull(result);
//        assertEquals(1, result.size());
//        verify(contractService, times(1)).getTenantList();
//    }
//
//    // ✅ ทดสอบ getOccupiedRooms()
//    @Test
//    void testGetOccupiedRooms() {
//        when(contractService.getOccupiedRoomIds()).thenReturn(List.of(101L, 102L));
//
//        List<Long> result = contractController.getOccupiedRooms();
//
//        assertNotNull(result);
//        assertEquals(2, result.size());
//        assertTrue(result.contains(101L));
//        assertTrue(result.contains(102L));
//        verify(contractService, times(1)).getOccupiedRoomIds();
//    }
//}
