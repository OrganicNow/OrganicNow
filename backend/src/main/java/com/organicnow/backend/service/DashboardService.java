package com.organicnow.backend.service;

import com.organicnow.backend.dto.DashboardDto;
import com.organicnow.backend.dto.FinanceMonthlyDto;
import com.organicnow.backend.dto.MaintainMonthlyDto;
import com.organicnow.backend.repository.InvoiceRepository;
import com.organicnow.backend.repository.RoomRepository;
import com.organicnow.backend.repository.ContractRepository;
import com.organicnow.backend.repository.MaintainRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final RoomRepository roomRepository;
    private final ContractRepository contractRepository;
    private final MaintainRepository maintainRepository;
    private final InvoiceRepository invoiceRepository;

    /** ✅ ห้องทั้งหมด (0=available, 1=unavailable, 2=repair) */
    public List<Map<String, Object>> getRoomStatuses() {
        return roomRepository.findAll().stream().map(r -> {
            Map<String, Object> map = new HashMap<>();

            // ✅ ดึงข้อมูลจาก Entity โดยตรง
            map.put("roomNumber", r.getRoomNumber());
            map.put("room_floor", r.getRoomFloor()); // 👈 ใช้ค่าจริงจาก DB

            // ✅ ตรวจสอบสถานะห้อง
            boolean hasContract = contractRepository.existsActiveContractByRoomId(r.getId());
            boolean hasMaintain = maintainRepository.existsActiveMaintainByRoomId(r.getId());

            if (hasContract) {
                map.put("status", 1); // มีผู้เช่า = unavailable
            } else if (hasMaintain) {
                map.put("status", 2); // อยู่ระหว่างซ่อม = repair
            } else {
                map.put("status", 0); // ว่าง = available
            }

            return map;
        }).toList();
    }

    /** ✅ ข้อมูลรีเควส 12 เดือนล่าสุด */
    public List<MaintainMonthlyDto> getMaintainRequests() {
        return maintainRepository.countRequestsLast12Months()
                .stream()
                .map(r -> new MaintainMonthlyDto(
                        (String) r[0],
                        ((Number) r[1]).longValue()
                ))
                .toList();
    }

    /** ✅ การเงินย้อนหลัง 12 เดือน */
    public List<FinanceMonthlyDto> getFinanceStats() {
        return invoiceRepository.countFinanceLast12Months()
                .stream()
                .map(r -> new FinanceMonthlyDto(
                        (String) r[0],
                        ((Number) r[1]).longValue(), // onTime
                        ((Number) r[2]).longValue(), // penalty
                        ((Number) r[3]).longValue()  // overdue
                ))
                .toList();
    }

    /** ✅ รวม Dashboard ทั้งหมด */
    public DashboardDto getDashboardData() {
        return new DashboardDto(
                getRoomStatuses(),
                getMaintainRequests(),
                getFinanceStats()
        );
    }
}
