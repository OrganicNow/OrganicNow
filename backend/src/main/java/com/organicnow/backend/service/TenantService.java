package com.organicnow.backend.service;

import com.organicnow.backend.dto.TenantDto;
import com.organicnow.backend.model.Tenant;
import com.organicnow.backend.repository.ContractRepository;
import com.organicnow.backend.repository.TenantRepository; // 🆕 import เพิ่ม
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TenantService {

    private final ContractRepository contractRepository;
    private final TenantRepository tenantRepository; // 🆕 เพิ่ม

    // 🆕 ปรับ constructor ให้รองรับ tenantRepository ด้วย
    public TenantService(ContractRepository contractRepository, TenantRepository tenantRepository) {
        this.contractRepository = contractRepository;
        this.tenantRepository = tenantRepository;
    }

    // ✅ ใช้สำหรับดึง tenant list (join contract + tenant + room + package)
    public Map<String, Object> list() {
        List<TenantDto> rows = contractRepository.findTenantRows();
        Map<String, Object> resp = new HashMap<>();
        resp.put("results", rows);
        resp.put("totalRecords", rows.size());
        return resp;
    }

    // 🆕 ใช้สำหรับ reindex ผู้เช่าทั้งหมด (ดึงจากฐานข้อมูล)
    public List<Tenant> getAllTenants() {
        return tenantRepository.findAll();
    }
}
