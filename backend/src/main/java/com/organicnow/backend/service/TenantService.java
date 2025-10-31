package com.organicnow.backend.service;

import com.organicnow.backend.dto.TenantDto;
import com.organicnow.backend.model.Tenant;
import com.organicnow.backend.repository.ContractRepository;
import com.organicnow.backend.repository.TenantRepository;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TenantService {

    private final ContractRepository contractRepository;
    private final TenantRepository tenantRepository;

    public TenantService(ContractRepository contractRepository, TenantRepository tenantRepository) {
        this.contractRepository = contractRepository;
        this.tenantRepository = tenantRepository;
    }

    // ✅ ดึงข้อมูลแบบ list ปกติ
    public Map<String, Object> list() {
        List<TenantDto> rows = contractRepository.findTenantRows();
        Map<String, Object> resp = new HashMap<>();
        resp.put("results", rows);
        resp.put("totalRecords", rows.size());
        return resp;
    }

    // ✅ Search ด้วย fuzzy (pg_trgm) + format เดิมของ list
    public Map<String, Object> searchTenantWithFuzzy(String keyword) {
        // 1️⃣ ดึง tenant ที่ fuzzy match
        List<Tenant> matchedTenants = tenantRepository.searchFuzzy(keyword);
        if (matchedTenants.isEmpty()) {
            return Map.of("results", List.of(), "totalRecords", 0);
        }

        // 2️⃣ เอา tenantId มาหา contract row
        List<Long> tenantIds = matchedTenants.stream()
                .map(Tenant::getId)
                .toList();

        // 3️⃣ ดึงข้อมูลเหมือน /tenant/list แต่เฉพาะ tenant ที่ match
        List<TenantDto> rows = contractRepository.findTenantRowsByTenantIds(tenantIds);

        Map<String, Object> resp = new HashMap<>();
        resp.put("results", rows);
        resp.put("totalRecords", rows.size());
        return resp;
    }
}