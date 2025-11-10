package com.organicnow.backend.service;

import com.organicnow.backend.dto.CreateMaintainRequest;
import com.organicnow.backend.dto.MaintainDto;
import com.organicnow.backend.dto.UpdateMaintainRequest;

import java.util.List;
import java.util.Optional;

public interface MaintainService {
    List<MaintainDto> getAll();
    Optional<MaintainDto> getById(Long id);
    MaintainDto create(CreateMaintainRequest req);
    MaintainDto update(Long id, UpdateMaintainRequest req);
    void delete(Long id);
    
    // ✅ เพิ่ม PDF generation
    byte[] generateMaintenanceReportPdf(Long maintainId);
}
