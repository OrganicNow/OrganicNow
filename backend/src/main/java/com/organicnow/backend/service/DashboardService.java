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

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final RoomRepository roomRepository;
    private final ContractRepository contractRepository;
    private final MaintainRepository maintainRepository;
    private final InvoiceRepository invoiceRepository;

    private static final DateTimeFormatter MONTH_FORMATTER =
            DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH);

    // ✅ ห้องทั้งหมด (0=available, 1=unavailable, 2=repair)
    public List<Map<String, Object>> getRoomStatuses() {
        return roomRepository.findAll().stream().map(r -> {
            Map<String, Object> map = new HashMap<>();
            map.put("roomNumber", r.getRoomNumber());
            map.put("room_floor", r.getRoomFloor());

            boolean hasContract = contractRepository.existsActiveContractByRoomId(r.getId());
            boolean hasMaintain = maintainRepository.existsActiveMaintainByRoomId(r.getId());

            if (hasContract) map.put("status", 1);
            else if (hasMaintain) map.put("status", 2);
            else map.put("status", 0);

            return map;
        }).toList();
    }

    // ✅ งานซ่อมย้อนหลัง 6 เดือนล่าสุด
    public List<MaintainMonthlyDto> getMaintainRequests() {
        List<Object[]> raw = maintainRepository.countRequestsLast12Months();
        if (raw.isEmpty()) return List.of();

        YearMonth now = YearMonth.now();
        List<YearMonth> last6 = new ArrayList<>();
        for (int i = 5; i >= 0; i--) last6.add(now.minusMonths(i));

        Map<YearMonth, Long> maintainMap = new HashMap<>();
        for (Object[] r : raw) {
            YearMonth ym = YearMonth.parse((String) r[0]);
            maintainMap.put(ym, ((Number) r[1]).longValue());
        }

        List<MaintainMonthlyDto> result = new ArrayList<>();
        for (YearMonth ym : last6) {
            result.add(new MaintainMonthlyDto(
                    ym.format(MONTH_FORMATTER),
                    maintainMap.getOrDefault(ym, 0L)
            ));
        }
        return result;
    }

    // ✅ การเงินย้อนหลัง 6 เดือนล่าสุด
    public List<FinanceMonthlyDto> getFinanceStats() {
        List<Object[]> raw = invoiceRepository.countFinanceLast12Months();
        if (raw.isEmpty()) return List.of();

        YearMonth now = YearMonth.now();
        List<YearMonth> last6 = new ArrayList<>();
        for (int i = 5; i >= 0; i--) last6.add(now.minusMonths(i));

        Map<YearMonth, long[]> financeMap = new HashMap<>();
        for (Object[] r : raw) {
            YearMonth ym = YearMonth.parse((String) r[0]);
            financeMap.put(ym, new long[]{
                    ((Number) r[1]).longValue(),
                    ((Number) r[2]).longValue(),
                    ((Number) r[3]).longValue()
            });
        }

        List<FinanceMonthlyDto> result = new ArrayList<>();
        for (YearMonth ym : last6) {
            long[] v = financeMap.getOrDefault(ym, new long[]{0, 0, 0});
            result.add(new FinanceMonthlyDto(
                    ym.format(MONTH_FORMATTER),
                    v[0], v[1], v[2]
            ));
        }
        return result;
    }

    // ✅ การใช้น้ำและไฟย้อนหลัง 6 เดือน
    public Map<String, Object> getRoomUsage() {
        List<Object[]> rawData = invoiceRepository.findRoomUsageSummary();
        Map<String, Map<YearMonth, Integer>> waterByRoomMonth = new HashMap<>();
        Map<String, Map<YearMonth, Integer>> elecByRoomMonth = new HashMap<>();

        for (Object[] row : rawData) {
            String roomNumber = (String) row[0];
            YearMonth ym = YearMonth.parse((String) row[1]);
            int waterUnit = ((Number) row[2]).intValue();
            int electricUnit = ((Number) row[3]).intValue();

            waterByRoomMonth.computeIfAbsent(roomNumber, k -> new HashMap<>()).put(ym, waterUnit);
            elecByRoomMonth.computeIfAbsent(roomNumber, k -> new HashMap<>()).put(ym, electricUnit);
        }

        YearMonth now = YearMonth.now();
        List<YearMonth> last6 = new ArrayList<>();
        for (int i = 5; i >= 0; i--) last6.add(now.minusMonths(i));

        Map<String, Object> finalResult = new LinkedHashMap<>();

        roomRepository.findAll().forEach(room -> {
            String roomNumber = room.getRoomNumber();
            Map<YearMonth, Integer> wm = waterByRoomMonth.getOrDefault(roomNumber, Collections.emptyMap());
            Map<YearMonth, Integer> em = elecByRoomMonth.getOrDefault(roomNumber, Collections.emptyMap());

            List<String> categories = new ArrayList<>();
            List<Integer> waterSeries = new ArrayList<>();
            List<Integer> elecSeries = new ArrayList<>();

            for (YearMonth ym : last6) {
                categories.add(ym.format(MONTH_FORMATTER));
                waterSeries.add(wm.getOrDefault(ym, 0));
                elecSeries.add(em.getOrDefault(ym, 0));
            }

            finalResult.put(roomNumber, Map.of(
                    "categories", categories,
                    "series", List.of(
                            Map.of("name", "Water (m³)", "data", waterSeries),
                            Map.of("name", "Electricity (kWh)", "data", elecSeries)
                    )
            ));
        });

        return finalResult;
    }

    // ✅ รวม Dashboard ทั้งหมด
    public DashboardDto getDashboardData() {
        return new DashboardDto(
                getRoomStatuses(),
                getMaintainRequests(),
                getFinanceStats(),
                getRoomUsage()
        );
    }

    // ✅ Export CSV ของเดือนที่เลือก (เช่น "Nov 2025" หรือ "2025-11")
    public List<String[]> exportMonthlyUsageCsv(String yearMonthStr) {
        // 🧩 รองรับทั้ง "Nov 2025" และ "2025-11"
        String yearMonthFormatted = normalizeYearMonth(yearMonthStr);

        List<Object[]> rows = invoiceRepository.findUsageByMonth(yearMonthFormatted);
        Map<String, Map<String, Object>> usageMap = new HashMap<>();

        for (Object[] r : rows) {
            String room = (String) r[0];
            String tenant = r[1] != null ? (String) r[1] : "ว่าง";
            String pkg = r[2] != null ? (String) r[2] : "-";
            int water = r[3] != null ? ((Number) r[3]).intValue() : 0;
            int elec = r[4] != null ? ((Number) r[4]).intValue() : 0;

            usageMap.put(room, Map.of(
                    "tenant", tenant,
                    "pkg", pkg,
                    "water", water,
                    "elec", elec
            ));
        }

        List<String[]> csv = new ArrayList<>();
        csv.add(new String[]{"Room", "Tenant", "Package", "Water (m³)", "Electricity (kWh)"});

        roomRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(r -> r.getRoomNumber()))
                .forEach(r -> {
                    String room = r.getRoomNumber();
                    Map<String, Object> data = usageMap.get(room);
                    csv.add(new String[]{
                            room,
                            data != null ? (String) data.get("tenant") : "ว่าง",
                            data != null ? (String) data.get("pkg") : "-",
                            data != null ? String.valueOf(data.get("water")) : "0",
                            data != null ? String.valueOf(data.get("elec")) : "0"
                    });
                });

        return csv;
    }

    // 🧩 แปลง "Nov 2025" → "2025-11" สำหรับใช้ query
    private String normalizeYearMonth(String input) {
        try {
            YearMonth ym = YearMonth.parse(input, MONTH_FORMATTER);
            return ym.toString(); // e.g. 2025-11
        } catch (DateTimeParseException e) {
            return input; // ถ้าแปลงไม่ได้ ก็ใช้ค่าที่รับมาโดยตรง
        }
    }
}
