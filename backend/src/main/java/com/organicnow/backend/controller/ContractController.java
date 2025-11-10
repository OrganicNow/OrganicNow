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

    // ✅ API สำหรับหา Contract จาก Floor และ Room - สำหรับ Outstanding Balance
    @GetMapping("/by-room")
    public TenantDto getContractByRoom(@RequestParam Integer floor, @RequestParam String room) {
        System.out.println("🔍 API /contract/by-room called with Floor: " + floor + ", Room: " + room);
        try {
            TenantDto result = contractService.findContractByFloorAndRoom(floor, room);
            System.out.println("✅ Found Contract ID: " + result.getContractId());
            return result;
        } catch (Exception e) {
            System.err.println("❌ Error in /contract/by-room: " + e.getMessage());
            throw e;
        }
    }

}