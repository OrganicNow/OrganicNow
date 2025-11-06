package com.organicnow.backend.controller;

import com.organicnow.backend.dto.RoomDetailDto;
import com.organicnow.backend.service.ContractService;
import com.organicnow.backend.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * LegacyApiController - ไฟล์นี้ไว้รองรับ API เก่าๆ ที่หน้าอื่นยังใช้อยู่
 * 
 * ทำไมต้องมีไฟล์นี้?
 * - ตอนแรกไม่มี แต่พอไปแก้ Invoice Management แล้วหน้าอื่นเริ่มพัง
 * - หน้า TenantManagement ยังเรียก /contracts/occupied-rooms อยู่ แต่เรา refactor API path ใหม่แล้ว
 * - ถ้าลบ path เก่าไป จะได้ 404 error แล้วหน้าพัง
 * - เลยทำไฟล์นี้ขึ้นมารองรับ path เก่าไว้ แทนที่จะไปแก้หลายหน้า
 * 
 * ไฟล์นี้ทำอะไร:
 * - รองรับ API path เก่าที่หน้าต่างๆ ยังใช้อยู่
 * - ป้องกัน 404 error ที่จะเกิดขึ้นกับหน้าที่ยังไม่ได้ update
 * - เป็น bridge ระหว่าง API เก่ากับใหม่ เพื่อให้ระบบทำงานได้ต่อเนื่อง
 */
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://app.localtest.me"}, allowCredentials = "true")
public class LegacyApiController {

    private final ContractService contractService;
    private final RoomService roomService;

    /**
     * API เดิมที่หน้า TenantManagement ยังใช้อยู่
     * ส่งรายการห้องที่มีคนเช่าอยู่แล้ว เพื่อไม่ให้เลือกซ้ำ
     */
    @GetMapping("/contracts/occupied-rooms")
    public List<Long> getOccupiedRoomsLegacy() {
        return contractService.getOccupiedRoomIds();
    }

    /**
     * API สำรองไว้ป้องกัน error หากหน้าไหนเรียก /contracts
     * ตอนนี้ยังไม่มีใครใช้ แต่เก็บไว้เผื่อ
     */
    @GetMapping("/contracts")
    public ResponseEntity<List<Object>> getContractsLegacy() {
        // ส่งของว่างไปก่อน ถ้าต้องการข้อมูลจริงค่อยมาแก้
        return ResponseEntity.ok(List.of());
    }
}