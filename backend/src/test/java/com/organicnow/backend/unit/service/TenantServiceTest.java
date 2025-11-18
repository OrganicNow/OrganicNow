package com.organicnow.backend.unit.service;

import com.organicnow.backend.dto.TenantDto;
import com.organicnow.backend.model.Tenant;
import com.organicnow.backend.repository.ContractRepository;
import com.organicnow.backend.repository.TenantRepository;
import com.organicnow.backend.service.TenantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TenantServiceTest {

    @Mock
    private ContractRepository contractRepository;

    @Mock
    private TenantRepository tenantRepository;

    @InjectMocks
    private TenantService tenantService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // ============================================================
    // 1) list()
    // ============================================================
    @Test
    void list_ShouldReturnRowsAndTotalRecords() {
        TenantDto dto1 = new TenantDto();
        TenantDto dto2 = new TenantDto();

        when(contractRepository.findTenantRows())
                .thenReturn(List.of(dto1, dto2));

        Map<String, Object> resp = tenantService.list();

        assertEquals(2, resp.get("totalRecords"));
        assertEquals(List.of(dto1, dto2), resp.get("results"));

        verify(contractRepository, times(1)).findTenantRows();
    }

    // ============================================================
    // 2) searchTenantWithFuzzy() → ไม่มี match
    // ============================================================
    @Test
    void searchTenantWithFuzzy_NoMatch_ShouldReturnEmpty() {
        when(tenantRepository.searchFuzzy("john")).thenReturn(List.of());

        Map<String, Object> resp = tenantService.searchTenantWithFuzzy("john");

        assertEquals(0, resp.get("totalRecords"));
        assertEquals(List.of(), resp.get("results"));

        verify(tenantRepository, times(1)).searchFuzzy("john");
        verify(contractRepository, never()).findTenantRowsByTenantIds(any());
    }

    // ============================================================
    // 3) searchTenantWithFuzzy() → มี match, return contract rows
    // ============================================================
    @Test
    void searchTenantWithFuzzy_WithMatch_ShouldReturnContractRows() {
        Tenant t1 = new Tenant();
        t1.setId(10L);
        Tenant t2 = new Tenant();
        t2.setId(20L);

        when(tenantRepository.searchFuzzy("ann"))
                .thenReturn(List.of(t1, t2));

        TenantDto dto1 = new TenantDto();
        TenantDto dto2 = new TenantDto();

        when(contractRepository.findTenantRowsByTenantIds(List.of(10L, 20L)))
                .thenReturn(List.of(dto1, dto2));

        Map<String, Object> resp = tenantService.searchTenantWithFuzzy("ann");

        assertEquals(2, resp.get("totalRecords"));
        assertEquals(List.of(dto1, dto2), resp.get("results"));

        verify(tenantRepository, times(1)).searchFuzzy("ann");
        verify(contractRepository, times(1))
                .findTenantRowsByTenantIds(List.of(10L, 20L));
    }
}
