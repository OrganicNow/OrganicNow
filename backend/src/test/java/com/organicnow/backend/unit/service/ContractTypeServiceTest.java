//package com.organicnow.backend.unit.service;
//
//import com.organicnow.backend.model.ContractType;
//import com.organicnow.backend.repository.ContractTypeRepository;
//import com.organicnow.backend.service.ContractTypeService;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.web.server.ResponseStatusException;
//
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//class ContractTypeServiceTest {
//
//    @Mock
//    private ContractTypeRepository contractTypeRepository;
//
//    @InjectMocks
//    private ContractTypeService contractTypeService;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    @Test
//    void testGetContractTypeById_found() {
//        ContractType type = ContractType.builder()
//                .id(1L)
//                .name("Monthly")
//                .build();
//
//        when(contractTypeRepository.findById(1L)).thenReturn(Optional.of(type));
//
//        ContractType result = contractTypeService.getContractTypeById(1L);
//
//        assertNotNull(result);
//        assertEquals("Monthly", result.getName());
//    }
//
//    @Test
//    void testGetContractTypeById_notFound() {
//        when(contractTypeRepository.findById(99L)).thenReturn(Optional.empty());
//
//        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
//                () -> contractTypeService.getContractTypeById(99L));
//
//        assertTrue(ex.getMessage().contains("ContractType not found with id 99"));
//    }
//}
