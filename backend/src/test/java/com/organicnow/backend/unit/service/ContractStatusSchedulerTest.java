//package com.organicnow.backend.unit.service;
//
//import com.organicnow.backend.repository.ContractRepository;
//import com.organicnow.backend.service.ContractStatusScheduler;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//
//import static org.mockito.Mockito.*;
//
//class ContractStatusSchedulerTest {
//
//    @Mock
//    private ContractRepository contractRepository;
//
//    @InjectMocks
//    private ContractStatusScheduler contractStatusScheduler;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    // ✅ กรณีมี expired contracts ถูกอัปเดต (updated > 0)
//    @Test
//    void testUpdateExpiredContracts_WithUpdates() {
//        when(contractRepository.updateExpiredContracts()).thenReturn(5);
//
//        // ไม่มีการ throw error
//        contractStatusScheduler.updateExpiredContracts();
//
//        verify(contractRepository, times(1)).updateExpiredContracts();
//    }
//
//    // ✅ กรณีไม่มี expired contracts ถูกอัปเดต (updated = 0)
//    @Test
//    void testUpdateExpiredContracts_NoUpdates() {
//        when(contractRepository.updateExpiredContracts()).thenReturn(0);
//
//        // เรียก method แล้วต้องไม่พัง
//        contractStatusScheduler.updateExpiredContracts();
//
//        verify(contractRepository, times(1)).updateExpiredContracts();
//    }
//}
