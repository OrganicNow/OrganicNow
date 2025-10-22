package com.organicnow.backend.service;

import com.organicnow.backend.model.TenantDocument;
import com.organicnow.backend.repository.TenantSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TenantSearchService {

    private final TenantSearchRepository tenantSearchRepository;

    // 🔍 ค้นหาจากชื่อหรือนามสกุล
    public List<TenantDocument> searchByName(String keyword) {
        return tenantSearchRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(keyword, keyword);
    }

    // 🧩 เพิ่มข้อมูลใหม่เข้า Elasticsearch
    public void indexTenant(TenantDocument doc) {
        tenantSearchRepository.save(doc);
    }

    // 🧩 Reindex ทั้งหมด
    public void indexAll(List<TenantDocument> docs) {
        tenantSearchRepository.saveAll(docs);
    }
}
