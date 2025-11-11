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

/**
 * ✅ Dashboard Service
 * ใช้สำหรับรวมข้อมูลทั้งหมดในหน้า Dashboard
 */
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
            map.put("room_floor", r.getRoomFloor()); // ใช้ค่าจริงจาก DB

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

    /** ✅ ข้อมูลรีเควสซ่อมย้อนหลัง 12 เดือน */
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

    /**
     * ✅ ดึงข้อมูลการใช้น้ำและไฟของทุกห้องจากตาราง invoice
     * ใช้ข้อมูลจาก invoice.requested_water_unit และ requested_electricity_unit
     * join ผ่าน contract → room
     */
    public Map<String, Object> getRoomUsage() {
        List<Object[]> rawData = invoiceRepository.findRoomUsageSummary();
        Map<String, Object> result = new LinkedHashMap<>();

        // ✅ รวมข้อมูลจาก invoice
        for (Object[] row : rawData) {
            String roomNumber = (String) row[0];
            String month = (String) row[1];
            Integer waterUnit = ((Number) row[2]).intValue();
            Integer electricUnit = ((Number) row[3]).intValue();

            // ถ้ายังไม่มีห้องนี้ใน result → สร้างใหม่
            result.computeIfAbsent(roomNumber, r -> {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("categories", new ArrayList<String>());
                map.put("water", new ArrayList<Integer>());
                map.put("electricity", new ArrayList<Integer>());
                return map;
            });

            @SuppressWarnings("unchecked")
            Map<String, Object> roomData = (Map<String, Object>) result.get(roomNumber);

            ((List<String>) roomData.get("categories")).add(month);
            ((List<Integer>) roomData.get("water")).add(waterUnit);
            ((List<Integer>) roomData.get("electricity")).add(electricUnit);
        }

        // ✅ ดึงเดือนทั้งหมดที่มีในข้อมูลจริง (เพื่อใช้กับห้องที่ไม่มีข้อมูล)
        Set<String> allMonths = new TreeSet<>();
        result.values().forEach(data -> {
            @SuppressWarnings("unchecked")
            List<String> cats = (List<String>) ((Map<String, Object>) data).get("categories");
            allMonths.addAll(cats);
        });

        // ✅ แปลงเป็นรูปแบบที่ ApexChart ใช้ได้ทันที
        Map<String, Object> finalResult = new LinkedHashMap<>();
        result.forEach((room, data) -> {
            finalResult.put(room, Map.of(
                    "categories", ((Map<?, ?>) data).get("categories"),
                    "series", List.of(
                            Map.of("name", "Water (m³)", "data", ((Map<?, ?>) data).get("water")),
                            Map.of("name", "Electricity (kWh)", "data", ((Map<?, ?>) data).get("electricity"))
                    )
            ));
        });

        // 🧩 เพิ่ม fallback ให้ทุกห้องที่ไม่มีข้อมูล invoice = 0 (แต่ใช้เดือนจริง)
        roomRepository.findAll().forEach(room -> {
            String roomNumber = room.getRoomNumber();
            if (!finalResult.containsKey(roomNumber)) {
                finalResult.put(roomNumber, Map.of(
                        "categories", allMonths.isEmpty() ? List.of() : new ArrayList<>(allMonths),
                        "series", List.of(
                                Map.of("name", "Water (m³)", "data", Collections.nCopies(allMonths.size(), 0)),
                                Map.of("name", "Electricity (kWh)", "data", Collections.nCopies(allMonths.size(), 0))
                        )
                ));
            }
        });

        return finalResult;
    }

    /** ✅ รวม Dashboard ทั้งหมด */
    public DashboardDto getDashboardData() {
        return new DashboardDto(
                getRoomStatuses(),      // ห้องกับสถานะ
                getMaintainRequests(),  // งานซ่อมย้อนหลัง
                getFinanceStats(),      // การเงินย้อนหลัง
                getRoomUsage()          // ✅ ข้อมูล usage น้ำ-ไฟ
        );
    }
}
