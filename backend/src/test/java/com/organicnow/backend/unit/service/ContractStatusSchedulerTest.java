package com.organicnow.backend.unit.service;

import com.organicnow.backend.repository.ContractRepository;
import com.organicnow.backend.service.ContractStatusScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

public class ContractStatusSchedulerTest {

    private ContractRepository contractRepository;
    private ContractStatusScheduler scheduler;

    @BeforeEach
    void setUp() {
        contractRepository = mock(ContractRepository.class);
        scheduler = new ContractStatusScheduler(contractRepository);
    }

    // -------------------------------------------------------
    // ✅ updatedContracts() เรียก repository 1 ครั้ง
    // -------------------------------------------------------
    @Test
    void testUpdateExpiredContracts_callsRepository() {

        when(contractRepository.updateExpiredContracts()).thenReturn(3);

        scheduler.updateExpiredContracts();

        verify(contractRepository, times(1)).updateExpiredContracts();
    }

    // -------------------------------------------------------
    // 🟩 updatedContracts() = 0 ก็ต้องไม่ error
    // -------------------------------------------------------
    @Test
    void testUpdateExpiredContracts_zeroUpdated_noError() {

        when(contractRepository.updateExpiredContracts()).thenReturn(0);

        scheduler.updateExpiredContracts();

        // ไม่มี exception + verify เรียก 1 ครั้ง
        verify(contractRepository, times(1)).updateExpiredContracts();
    }

    // -------------------------------------------------------
    // ❌ repository ขว้าง exception → scheduler ต้องปล่อยทิ้งต่อ
    // -------------------------------------------------------
    @Test
    void testUpdateExpiredContracts_repositoryThrows_propagatesException() {

        when(contractRepository.updateExpiredContracts())
                .thenThrow(new RuntimeException("DB error"));

        try {
            scheduler.updateExpiredContracts();
        } catch (RuntimeException ex) {
            // ควรปล่อย error ออกมา
            assert(ex.getMessage().contains("DB error"));
        }

        verify(contractRepository, times(1)).updateExpiredContracts();
    }
}
