package com.organicnow.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDto {
    private List<Map<String, Object>> rooms;          // ห้องกับสถานะ
    private List<MaintainMonthlyDto> maintains;       // รีเควสซ่อมย้อนหลัง 12 เดือน
    private List<FinanceMonthlyDto> finances;         // การเงินย้อนหลัง 12 เดือน
    private Map<String, Object> usages;               // ✅ การใช้น้ำ/ไฟของแต่ละห้อง
}
