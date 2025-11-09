package com.organicnow.backend.controller;

import com.organicnow.backend.dto.TenantDto;
import com.organicnow.backend.service.ContractService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/contract") // เปลี่ยนจาก /contracts เป็น /contract
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://app.localtest.me"}, allowCredentials = "true")
public class ContractController {

    private final ContractService contractService;

    // ✅ API สำหรับ frontend invoice management - GET /contract/list
    @GetMapping("/list")
    public List<TenantDto> getContractList() {
        return contractService.getTenantList();
    }

    // ✅ API ดึง tenant list
    @GetMapping("/tenant/list")
    public List<TenantDto> getTenantList() {
        return contractService.getTenantList();
    }

    // ✅ API ดึงห้องที่ยัง occupied จริง ๆ
    @GetMapping("/occupied-rooms")
    public List<Long> getOccupiedRooms() {
        return contractService.getOccupiedRoomIds();
    }

}