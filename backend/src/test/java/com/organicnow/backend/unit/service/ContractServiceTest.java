//package com.organicnow.backend.unit.service;
//
//import com.organicnow.backend.dto.TenantDto;
//import com.organicnow.backend.repository.ContractRepository;
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
//class ContractServiceTest {
//
//    @Mock
//    private ContractRepository contractRepository;
//
//    @InjectMocks
//    private ContractService contractService;
//
//    private TenantDto sampleTenant;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//        sampleTenant = new TenantDto(); // ไม่ต้องตั้งค่า field แล้ว
//    }
//
//    // ✅ getTenantList()
//    @Test
//    void testGetTenantList() {
//        when(contractRepository.findTenantRows()).thenReturn(List.of(sampleTenant));
//
//        List<TenantDto> result = contractService.getTenantList();
//
//        assertNotNull(result);
//        assertEquals(1, result.size()); // ✅ ตรวจแค่จำนวน ไม่อ่าน field ภายใน
//        verify(contractRepository, times(1)).findTenantRows();
//    }
//
//    // ✅ getOccupiedRoomIds()
//    @Test
//    void testGetOccupiedRoomIds() {
//        when(contractRepository.findCurrentlyOccupiedRoomIds()).thenReturn(List.of(101L, 102L));
//
//        List<Long> result = contractService.getOccupiedRoomIds();
//
//        assertNotNull(result);
//        assertEquals(2, result.size());
//        assertTrue(result.contains(101L));
//        assertTrue(result.contains(102L));
//        verify(contractRepository, times(1)).findCurrentlyOccupiedRoomIds();
//    }
//}
