package com.organicnow.backend.service;

import com.organicnow.backend.dto.TenantDto;
import com.organicnow.backend.model.Contract;
import com.organicnow.backend.repository.ContractRepository;
import com.organicnow.backend.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContractService {

    private final ContractRepository contractRepository;
    private final RoomRepository roomRepository;

    // ✅ method ดึง tenant list
    public List<TenantDto> getTenantList() {
        return contractRepository.findTenantRows();
    }

    // ✅ method เดิม (occupied rooms)
    public List<Long> getOccupiedRoomIds() {
        return contractRepository.findCurrentlyOccupiedRoomIds();
    }

    // ✅ หา Contract จาก Floor และ Room สำหรับ Outstanding Balance
    public TenantDto findContractByFloorAndRoom(Integer floor, String room) {
        try {
            // ใช้ roomRepository เพื่อหา Contract ปัจจุบันจาก Floor และ Room
            Contract contract = roomRepository.findCurrentContractByRoomFloorAndNumber(floor, room);
            
            if (contract == null) {
                throw new RuntimeException("ไม่พบสัญญาสำหรับห้อง Floor " + floor + " Room " + room);
            }
            
            // แปลง Contract เป็น TenantDto
            return TenantDto.builder()
                    .contractId(contract.getId())
                    .firstName(contract.getTenant() != null ? contract.getTenant().getFirstName() : "N/A")
                    .lastName(contract.getTenant() != null ? contract.getTenant().getLastName() : "")
                    .floor(contract.getRoom() != null ? contract.getRoom().getRoomFloor() : floor)
                    .room(contract.getRoom() != null ? contract.getRoom().getRoomNumber() : room)
                    .roomId(contract.getRoom() != null ? contract.getRoom().getId() : null)
                    .build();
                    
        } catch (Exception e) {
            throw new RuntimeException("เกิดข้อผิดพลาดในการค้นหาสัญญา: " + e.getMessage());
        }
    }
}