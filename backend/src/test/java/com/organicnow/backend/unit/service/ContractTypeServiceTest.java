package com.organicnow.backend.unit.service;

import com.organicnow.backend.model.ContractType;
import com.organicnow.backend.repository.ContractTypeRepository;
import com.organicnow.backend.service.ContractTypeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ContractTypeServiceTest {

    private ContractTypeRepository contractTypeRepository;
    private ContractTypeService contractTypeService;

    @BeforeEach
    void setUp() {
        contractTypeRepository = mock(ContractTypeRepository.class);
        contractTypeService = new ContractTypeService(contractTypeRepository);
    }

    // ----------------------------------------------------------
    // ✅ getAllContractTypes()
    // ----------------------------------------------------------
    @Test
    void testGetAllContractTypes() {
        ContractType t1 = ContractType.builder()
                .id(1L)
                .name("Monthly")
                .duration(12)
                .build();

        ContractType t2 = ContractType.builder()
                .id(2L)
                .name("Yearly")
                .duration(24)
                .build();

        when(contractTypeRepository.findAll()).thenReturn(Arrays.asList(t1, t2));

        List<ContractType> result = contractTypeService.getAllContractTypes();

        assertEquals(2, result.size());
        verify(contractTypeRepository, times(1)).findAll();
    }

    // ----------------------------------------------------------
    // ✅ getContractTypeById() — found
    // ----------------------------------------------------------
    @Test
    void testGetContractTypeById_found() {
        ContractType mockType = ContractType.builder()
                .id(1L)
                .name("Monthly")
                .duration(12)
                .build();

        when(contractTypeRepository.findById(1L)).thenReturn(Optional.of(mockType));

        ContractType result = contractTypeService.getContractTypeById(1L);

        assertNotNull(result);
        assertEquals("Monthly", result.getName());
        verify(contractTypeRepository, times(1)).findById(1L);
    }

    // ----------------------------------------------------------
    // ❌ getContractTypeById() — not found → throw 404
    // ----------------------------------------------------------
    @Test
    void testGetContractTypeById_notFound() {
        when(contractTypeRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> contractTypeService.getContractTypeById(99L)
        );

        assertTrue(ex.getMessage().contains("ContractType not found with id 99"));
        verify(contractTypeRepository, times(1)).findById(99L);
    }

    // ----------------------------------------------------------
    // ✅ createContractType()
    // ----------------------------------------------------------
    @Test
    void testCreateContractType() {
        ContractType mockType = ContractType.builder()
                .id(1L)
                .name("Monthly")
                .duration(12)
                .build();

        when(contractTypeRepository.save(mockType)).thenReturn(mockType);

        ContractType result = contractTypeService.createContractType(mockType);

        assertEquals("Monthly", result.getName());
        verify(contractTypeRepository, times(1)).save(mockType);
    }

    // ----------------------------------------------------------
    // ❌ deleteContractType() — not found
    // ----------------------------------------------------------
    @Test
    void testDeleteContractType_notFound() {
        when(contractTypeRepository.existsById(99L)).thenReturn(false);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> contractTypeService.deleteContractType(99L)
        );

        assertTrue(ex.getReason().contains("ContractType not found with id 99"));
        verify(contractTypeRepository, times(1)).existsById(99L);
        verify(contractTypeRepository, never()).deleteById(any());
    }

    // ----------------------------------------------------------
    // ✅ deleteContractType() — success
    // ----------------------------------------------------------
    @Test
    void testDeleteContractType_success() {
        when(contractTypeRepository.existsById(1L)).thenReturn(true);

        contractTypeService.deleteContractType(1L);

        verify(contractTypeRepository, times(1)).existsById(1L);
        verify(contractTypeRepository, times(1)).deleteById(1L);
    }
}
