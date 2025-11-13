//package com.organicnow.backend.unit.service;
//
//import com.organicnow.backend.dto.TenantDto;
//import com.organicnow.backend.repository.ContractRepository;
//import com.organicnow.backend.service.TenantService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Map;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//class TenantServiceTest {
//
//    @Mock
//    private ContractRepository contractRepository;
//
//    @InjectMocks
//    private TenantService tenantService;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    @Test
//    void list_shouldReturnResultsAndTotalRecords() {
//        TenantDto tenant1 = TenantDto.builder()
//                .contractId(1L)
//                .firstName("John")
//                .lastName("Doe")
//                .floor(1)
//                .room("101")
//                .packageId(10L)
//                .startDate(LocalDateTime.now())
//                .endDate(LocalDateTime.now().plusMonths(6))
//                .phoneNumber("0812345678")
//                .email("john@example.com")
//                .build();
//
//        TenantDto tenant2 = TenantDto.builder()
//                .contractId(2L)
//                .firstName("Jane")
//                .lastName("Smith")
//                .floor(2)
//                .room("202")
//                .packageId(11L)
//                .startDate(LocalDateTime.now())
//                .endDate(LocalDateTime.now().plusMonths(12))
//                .phoneNumber("0898765432")
//                .email("jane@example.com")
//                .build();
//
//        when(contractRepository.findTenantRows()).thenReturn(List.of(tenant1, tenant2));
//
//        // Act
//        Map<String, Object> result = tenantService.list();
//
//        // Assert
//        assertNotNull(result);
//        assertTrue(result.containsKey("results"));
//        assertTrue(result.containsKey("totalRecords"));
//
//        @SuppressWarnings("unchecked")
//        List<TenantDto> tenants = (List<TenantDto>) result.get("results");
//
//        assertEquals(2, tenants.size());
//        assertEquals(2, result.get("totalRecords"));
//
//        assertEquals("John", tenants.get(0).getFirstName());
//        assertEquals("Jane", tenants.get(1).getFirstName());
//
//        verify(contractRepository, times(1)).findTenantRows();
//    }
//}
