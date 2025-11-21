package com.organicnow.backend.controller;

import com.organicnow.backend.dto.DashboardDto;
import com.organicnow.backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * ✅ Dashboard Controller — รองรับทั้ง "Nov 2025", "Nov_2025" หรือ "2025-11"
 */
@RestController
@RequestMapping({"/dashboard", "/api/dashboard"})
@CrossOrigin(
        origins = {"http://localhost:5173",
                "http://app.localtest.me",
                "https://transcondylar-noncorporately-christen.ngrok-free.dev"},
        allowCredentials = "true"
)
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /** ✅ โหลดข้อมูล Dashboard หลัก */
    @GetMapping
    public DashboardDto getDashboardData() {
        return dashboardService.getDashboardData();
    }

    /** ✅ ดาวน์โหลด CSV รายเดือน (ยืดหยุ่นเรื่องรูปแบบเดือน) */
    @GetMapping("/export/{yearMonth}")
    public ResponseEntity<byte[]> exportMonthlyCsv(@PathVariable String yearMonth) {
        String normalizedMonth = yearMonth.replace("_", " ");
        List<String[]> csvData = dashboardService.exportMonthlyUsageCsv(normalizedMonth);

        // ✅ ใส่ BOM (UTF-8 Signature)
        byte[] bom = new byte[] {(byte)0xEF, (byte)0xBB, (byte)0xBF};

        // ✅ สร้างเนื้อหา CSV ด้วย StringBuilder
        StringBuilder sb = new StringBuilder();
        csvData.forEach(row -> sb.append(String.join(",", row)).append("\n"));
        byte[] csvBody = sb.toString().getBytes(StandardCharsets.UTF_8);

        // ✅ รวม BOM กับเนื้อหา
        byte[] csvBytes = new byte[bom.length + csvBody.length];
        System.arraycopy(bom, 0, csvBytes, 0, bom.length);
        System.arraycopy(csvBody, 0, csvBytes, bom.length, csvBody.length);

        // ✅ ตั้งชื่อไฟล์ให้อ่านง่าย
        String fileName = "Usage_Report_" + normalizedMonth.replace(" ", "_") + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csvBytes);
    }


    /**
     * ✅ Helper: แปลง month ที่รับมาให้เป็น "YYYY-MM"
     * รองรับ input:
     *  - "Nov 2025"
     *  - "Nov_2025"
     *  - "2025-11"
     */
    private String normalizeToYearMonth(String input) {
        if (input == null || input.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Month is required");
        }

        // 1️⃣ ถ้ามาเป็น YYYY-MM อยู่แล้ว
        if (input.matches("\\d{4}-\\d{2}")) {
            return input;
        }

        // 2️⃣ แปลง underscore → space
        String cleaned = input.replace("_", " ").trim();

        // 3️⃣ พยายาม parse "MMM yyyy"
        try {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH);
            YearMonth ym = YearMonth.parse(cleaned, fmt);
            return ym.toString(); // "2025-11"
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid month format: " + input + ". Use 'YYYY-MM' or 'MMM yyyy'.");
        }
    }
}
