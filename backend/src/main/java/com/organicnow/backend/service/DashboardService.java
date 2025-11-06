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

    // ✅ ห้องทั้งหมด (0=available, 1=unavailable, 2=repair)
    public List<Map<String, Object>> getRoomStatuses() {
        return roomRepository.findAll().stream().map(r -> {
            Map<String, Object> map = new HashMap<>();
            map.put("roomNumber", r.getRoomNumber());

            // ✅ หา floor: ถ้ามี field ใน entity Room ให้ใช้ getFloor() ได้เลย
            Integer floor = null;
            try {
                // ถ้า Room entity มี field floor อยู่แล้วให้เปิดบรรทัดนี้แทน
                // floor = r.getFloor();

                if (floor == null) {
                    floor = deriveFloorFromRoomNumber(r.getRoomNumber());
                }
            } catch (Exception ignored) { /* ignore */ }

            map.put("room_floor", floor); // 👈 เพิ่มคีย์ใหม่ส่งไป frontend

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

    /** 🧮 Helper: คำนวณชั้นจากหมายเลขห้อง เช่น "101" → 1, "212" → 2 */
    private Integer deriveFloorFromRoomNumber(Object roomNumber) {
        if (roomNumber == null) return null;
        String s = String.valueOf(roomNumber).trim();

        // ดึงเฉพาะตัวเลขนำหน้า เช่น "201A" -> "201"
        StringBuilder digits = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (Character.isDigit(c)) digits.append(c);
            else break;
        }

        if (digits.length() == 0) return null;

        // สมมติรูปแบบรหัสห้องคือ 1xx / 2xx / 3xx → ใช้เลขหลักแรกเป็นชั้น
        return Character.getNumericValue(digits.charAt(0));
    }

    // ✅ ข้อมูลรีเควส 12 เดือนล่าสุด
    public List<MaintainMonthlyDto> getMaintainRequests() {
        return maintainRepository.countRequestsLast12Months()
                .stream()
                .map(r -> new MaintainMonthlyDto((String) r[0], (Long) r[1]))
                .toList();
    }

    // ✅ การเงินย้อนหลัง 12 เดือน
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

    // ✅ รวม Dashboard ทั้งหมด
    public DashboardDto getDashboardData() {
        return new DashboardDto(
                getRoomStatuses(),
                getMaintainRequests(),
                getFinanceStats()
        );
    }
}
