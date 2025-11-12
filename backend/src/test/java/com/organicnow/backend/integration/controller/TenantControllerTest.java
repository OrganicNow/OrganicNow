//package com.organicnow.backend.controller;
//
//import com.organicnow.backend.dto.CreateTenantContractRequest;
//import com.organicnow.backend.dto.TenantDto;
//import com.organicnow.backend.dto.UpdateTenantContractRequest;
//import com.organicnow.backend.service.TenantContractService;
//import com.organicnow.backend.service.TenantService;
//import com.organicnow.backend.controller.TenantController;
//
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.http.ResponseEntity;
//
//import java.time.LocalDateTime;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//class TenantControllerTest {
//
//    @Mock
//    private TenantService tenantService;
//
//    @Mock
//    private TenantContractService tenantContractService;
//
//    @InjectMocks
//    private TenantController tenantController;
//
//    private AutoCloseable closeable;
//
//    @BeforeEach
//    void setUp() {
//        closeable = MockitoAnnotations.openMocks(this);
//    }
//
//    @AfterEach
//    void tearDown() throws Exception {
//        closeable.close();
//    }
//
//    @Test
//    void testListTenants() {
//        Map<String, Object> mockResponse = new HashMap<>();
//        mockResponse.put("results", Collections.emptyList());
//        mockResponse.put("totalRecords", 0);
//
//        when(tenantService.list()).thenReturn(mockResponse);
//
//        ResponseEntity<?> response = tenantController.list();
//
//        assertEquals(200, response.getStatusCode().value());
//
//        Map<String, Object> body = (Map<String, Object>) response.getBody();
//        assertNotNull(body);
//        assertTrue(((List<?>) body.get("results")).isEmpty());
//        assertEquals(0, body.get("totalRecords"));
//
//        verify(tenantService, times(1)).list();
//    }
//
//    @Test
//    void testCreateTenant() {
//        CreateTenantContractRequest req = new CreateTenantContractRequest();
//        TenantDto dto = TenantDto.builder()
//                .contractId(1L)
//                .firstName("John")
//                .lastName("Doe")
//                .startDate(LocalDateTime.now())
//                .endDate(LocalDateTime.now().plusMonths(6))
//                .build();
//
//        when(tenantContractService.create(req)).thenReturn(dto);
//
//        ResponseEntity<TenantDto> response = tenantController.create(req);
//
//        assertEquals(201, response.getStatusCode().value());
//        assertEquals("John", response.getBody().getFirstName());
//        verify(tenantContractService, times(1)).create(req);
//    }
//
//    @Test
//    void testUpdateTenant() {
//        Long contractId = 1L;
//        UpdateTenantContractRequest req = new UpdateTenantContractRequest();
//        TenantDto updated = TenantDto.builder()
//                .contractId(contractId)
//                .firstName("Jane")
//                .lastName("Smith")
//                .build();
//
//        when(tenantContractService.update(contractId, req)).thenReturn(updated);
//
//        ResponseEntity<TenantDto> response = tenantController.update(contractId, req);
//
//        assertEquals(200, response.getStatusCode().value());
//        assertEquals("Jane", response.getBody().getFirstName());
//        verify(tenantContractService, times(1)).update(contractId, req);
//    }
//
//    @Test
//    void testDeleteTenant() {
//        Long contractId = 1L;
//
//        doNothing().when(tenantContractService).delete(contractId);
//
//        ResponseEntity<Void> response = tenantController.delete(contractId);
//
//        assertEquals(204, response.getStatusCode().value());
//        verify(tenantContractService, times(1)).delete(contractId);
//    }
//}
