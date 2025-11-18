package com.organicnow.backend.integration.service;

import com.organicnow.backend.dto.FinanceMonthlyDto;
import com.organicnow.backend.dto.MaintainMonthlyDto;
import com.organicnow.backend.service.DashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.PostgreSQLContainer;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
class DashboardServiceIntegrationTest {

    @ServiceConnection
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final DateTimeFormatter MONTH_FORMATTER =
            DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH);

    @BeforeEach
    void cleanDatabase() {
        // ล้างข้อมูลทุกอย่างให้ DB สะอาดก่อนเทสแต่ละอัน
        jdbcTemplate.execute("""
            TRUNCATE TABLE
                payment_proofs,
                payment_records,
                invoice_item,
                invoice,
                maintain,
                contract_file,
                contract,
                tenant,
                room_asset,
                room,
                asset_event,
                asset,
                asset_group,
                package_plan,
                contract_type,
                admin
            RESTART IDENTITY CASCADE
            """);
    }

    // -------------------------------------------------------------
    // 1) getRoomStatuses()
    // -------------------------------------------------------------
    @Test
    void getRoomStatuses_shouldReturnCorrectStatusesForEachRoom() {
        // room: 101 = occupied, 102 = repair, 201 = available

        // 1) สร้างห้อง
        jdbcTemplate.update("INSERT INTO room (room_floor, room_size, room_number) VALUES (1, 30, '101')");
        jdbcTemplate.update("INSERT INTO room (room_floor, room_size, room_number) VALUES (1, 30, '102')");
        jdbcTemplate.update("INSERT INTO room (room_floor, room_size, room_number) VALUES (2, 30, '201')");

        // ตอนนี้ room_id: 1 -> 101, 2 -> 102, 3 -> 201 (เพราะ identity reset แล้ว)

        // 2) contract active ให้ห้อง 101
        jdbcTemplate.update("INSERT INTO contract_type (duration, contract_name) VALUES (12, 'Standard')");
        jdbcTemplate.update("INSERT INTO package_plan (is_active, price, room_size, contract_type_id) " +
                "VALUES (1, 5000, 30, 1)");
        jdbcTemplate.update("""
            INSERT INTO tenant (national_id, first_name, last_name, phone_number, email)
            VALUES ('1111111111111', 'Foo', 'Bar', '000', 'foo@example.com')
            """);

        jdbcTemplate.update("""
            INSERT INTO contract (
                deposit, rent_amount_snapshot, status,
                package_id, room_id, tenant_id,
                sign_date, start_date, end_date
            )
            VALUES (
                20000, 5000, 1,
                1, 1, 1,
                NOW(), NOW() - INTERVAL '1 month', NOW() + INTERVAL '1 month'
            )
            """);

        // 3) maintain active (finish_date = null) ให้ห้อง 102
        jdbcTemplate.update("""
            INSERT INTO maintain (
                issue_category, target_type, create_date,
                room_id, issue_title
            )
            VALUES (0, 0, NOW(), 2, 'Leak in bathroom')
            """);

        // 4) เรียก service
        List<Map<String, Object>> roomStatuses = dashboardService.getRoomStatuses();

        // แปลงเป็น map: roomNumber -> status
        Map<String, Integer> statusByRoom = roomStatuses.stream()
                .collect(Collectors.toMap(
                        m -> (String) m.get("roomNumber"),
                        m -> (Integer) m.get("status")
                ));

        // ตรวจว่ามีครบ 3 ห้อง
        assertThat(statusByRoom).hasSize(3);

        // 101 มี active contract → status = 1
        assertThat(statusByRoom.get("101")).isEqualTo(1);

        // 102 มี maintain ที่ยังไม่เสร็จ → status = 2
        assertThat(statusByRoom.get("102")).isEqualTo(2);

        // 201 ไม่มีทั้ง contract และ maintain → status = 0
        assertThat(statusByRoom.get("201")).isEqualTo(0);
    }

    // -------------------------------------------------------------
    // 2) getMaintainRequests()
    // -------------------------------------------------------------
    @Test
    void getMaintainRequests_shouldReturnLast6MonthsWithCorrectCounts() {
        // ต้องมีห้องอย่างน้อย 1 ห้องก่อน (foreign key)
        jdbcTemplate.update("INSERT INTO room (room_floor, room_size, room_number) VALUES (1, 30, '101')");

        YearMonth now = YearMonth.now();
        YearMonth lastMonth = now.minusMonths(1);
        YearMonth threeMonthsAgo = now.minusMonths(3);

        // maintain 2 ตัวใน lastMonth
        jdbcTemplate.update("""
            INSERT INTO maintain (issue_category, target_type, create_date, room_id, issue_title)
            VALUES (0, 0,
                    TO_TIMESTAMP(? || '-15 10:00:00', 'YYYY-MM-DD HH24:MI:SS'),
                    1, 'Issue LM 1')
            """, lastMonth.toString());
        jdbcTemplate.update("""
            INSERT INTO maintain (issue_category, target_type, create_date, room_id, issue_title)
            VALUES (0, 0,
                    TO_TIMESTAMP(? || '-20 12:00:00', 'YYYY-MM-DD HH24:MI:SS'),
                    1, 'Issue LM 2')
            """, lastMonth.toString());

        // maintain 1 ตัวใน threeMonthsAgo
        jdbcTemplate.update("""
            INSERT INTO maintain (issue_category, target_type, create_date, room_id, issue_title)
            VALUES (0, 0,
                    TO_TIMESTAMP(? || '-05 09:00:00', 'YYYY-MM-DD HH24:MI:SS'),
                    1, 'Issue 3M ago')
            """, threeMonthsAgo.toString());

        // เรียก service
        List<MaintainMonthlyDto> maintainStats = dashboardService.getMaintainRequests();

        // ควรมี 6 เดือนล่าสุด
        assertThat(maintainStats).hasSize(6);

        // แปลงเป็น map: label "MMM yyyy" -> total
        Map<String, Long> totalByLabel = maintainStats.stream()
                .collect(Collectors.toMap(
                        MaintainMonthlyDto::getMonth,
                        MaintainMonthlyDto::getTotal

                ));

        String lastMonthLabel = lastMonth.format(MONTH_FORMATTER);
        String threeMonthsAgoLabel = threeMonthsAgo.format(MONTH_FORMATTER);

        // เดือนที่เราใส่ 2 เคส
        assertThat(totalByLabel.get(lastMonthLabel)).isEqualTo(2L);

        // เดือนที่เราใส่ 1 เคส
        assertThat(totalByLabel.get(threeMonthsAgoLabel)).isEqualTo(1L);

        // เดือนอื่น ๆ ใน 6 เดือนล่าสุดต้องไม่เป็น null (ถูกเติม 0 ไว้)
        maintainStats.forEach(dto -> assertThat(dto.getTotal()).isNotNull());
    }

    // -------------------------------------------------------------
    // 3) getFinanceStats()
    // -------------------------------------------------------------
    @Test
    void getFinanceStats_shouldSummarizeOnTimePenaltyAndOverdueCorrectly() {
        // เตรียม contract + room + tenant ให้พร้อมสำหรับ invoice
        jdbcTemplate.update("INSERT INTO room (room_floor, room_size, room_number) VALUES (1, 30, '101')");
        jdbcTemplate.update("""
            INSERT INTO tenant (national_id, first_name, last_name, phone_number, email)
            VALUES ('2222222222222', 'Alice', 'Tenant', '001', 'alice@example.com')
            """);
        jdbcTemplate.update("INSERT INTO contract_type (duration, contract_name) VALUES (12, 'FinanceTest')");
        jdbcTemplate.update("""
            INSERT INTO package_plan (is_active, price, room_size, contract_type_id)
            VALUES (1, 7000, 30, 1)
            """);
        jdbcTemplate.update("""
            INSERT INTO contract (
                deposit, rent_amount_snapshot, status,
                package_id, room_id, tenant_id,
                sign_date, start_date, end_date
            )
            VALUES (
                10000, 7000, 1,
                1, 1, 1,
                NOW(), NOW() - INTERVAL '1 month', NOW() + INTERVAL '2 month'
            )
            """);

        YearMonth now = YearMonth.now();
        YearMonth lastMonth = now.minusMonths(1);

        // invoice 3 ใบในเดือนเดียวกัน:
        // 1) onTime: status=1, penalty_total = 0
        // 2) penalty: status=1, penalty_total > 0
        // 3) overdue: status=0
        // ใช้ date ภายในเดือน lastMonth
        String lastMonthBase = lastMonth.toString(); // "YYYY-MM"

        // onTime
        jdbcTemplate.update("""
            INSERT INTO invoice (
                invoice_status, net_amount, penalty_total, sub_total,
                contract_id, create_date, due_date
            )
            VALUES (
                1, 7000, 0, 7000,
                1,
                TO_TIMESTAMP(? || '-05 10:00:00', 'YYYY-MM-DD HH24:MI:SS'),
                TO_TIMESTAMP(? || '-25 23:59:59', 'YYYY-MM-DD HH24:MI:SS')
            )
            """, lastMonthBase, lastMonthBase);

        // penalty
        jdbcTemplate.update("""
            INSERT INTO invoice (
                invoice_status, net_amount, penalty_total, sub_total,
                contract_id, create_date, due_date
            )
            VALUES (
                1, 7100, 100, 7000,
                1,
                TO_TIMESTAMP(? || '-10 10:00:00', 'YYYY-MM-DD HH24:MI:SS'),
                TO_TIMESTAMP(? || '-25 23:59:59', 'YYYY-MM-DD HH24:MI:SS')
            )
            """, lastMonthBase, lastMonthBase);

        // overdue
        jdbcTemplate.update("""
            INSERT INTO invoice (
                invoice_status, net_amount, penalty_total, sub_total,
                contract_id, create_date, due_date
            )
            VALUES (
                0, 7000, 0, 7000,
                1,
                TO_TIMESTAMP(? || '-15 10:00:00', 'YYYY-MM-DD HH24:MI:SS'),
                TO_TIMESTAMP(? || '-20 23:59:59', 'YYYY-MM-DD HH24:MI:SS')
            )
            """, lastMonthBase, lastMonthBase);

        // เรียก service
        List<FinanceMonthlyDto> financeStats = dashboardService.getFinanceStats();

        // ต้องมี 6 เดือนล่าสุด
        assertThat(financeStats).hasSize(6);

        Map<String, FinanceMonthlyDto> dtoByLabel = financeStats.stream()
                .collect(Collectors.toMap(
                        FinanceMonthlyDto::getMonth,
                        dto -> dto
                ));

        String lastMonthLabel = lastMonth.format(MONTH_FORMATTER);
        FinanceMonthlyDto lastMonthDto = dtoByLabel.get(lastMonthLabel);

        assertThat(lastMonthDto).isNotNull();
        assertThat(lastMonthDto.getOnTime()).isEqualTo(1L);
        assertThat(lastMonthDto.getPenalty()).isEqualTo(1L);
        assertThat(lastMonthDto.getOverdue()).isEqualTo(1L);

        // เดือนอื่น ๆ มีค่าอย่างน้อยต้องไม่ null
        financeStats.forEach(dto -> {
            assertThat(dto.getOnTime()).isNotNull();
            assertThat(dto.getPenalty()).isNotNull();
            assertThat(dto.getOverdue()).isNotNull();
        });
    }

    // -------------------------------------------------------------
    // 4) getRoomUsage()
    // -------------------------------------------------------------
    @Test
    void getRoomUsage_shouldReturnWaterAndElectricitySeriesForEachRoom() {
        // สร้าง room + contract แล้วออก invoice มี requested_xxx_unit
        jdbcTemplate.update("INSERT INTO room (room_floor, room_size, room_number) VALUES (1, 30, '101')");
        jdbcTemplate.update("""
            INSERT INTO tenant (national_id, first_name, last_name, phone_number, email)
            VALUES ('3333333333333', 'Bob', 'User', '002', 'bob@example.com')
            """);
        jdbcTemplate.update("INSERT INTO contract_type (duration, contract_name) VALUES (12, 'UsageTest')");
        jdbcTemplate.update("""
            INSERT INTO package_plan (is_active, price, room_size, contract_type_id)
            VALUES (1, 8000, 30, 1)
            """);
        jdbcTemplate.update("""
            INSERT INTO contract (
                deposit, rent_amount_snapshot, status,
                package_id, room_id, tenant_id,
                sign_date, start_date, end_date
            )
            VALUES (
                8000, 8000, 1,
                1, 1, 1,
                NOW(), NOW() - INTERVAL '2 month', NOW() + INTERVAL '4 month'
            )
            """);

        YearMonth now = YearMonth.now();
        YearMonth lastMonth = now.minusMonths(1);
        String lastMonthBase = lastMonth.toString();

        // ใบ invoice ใน lastMonth: waterUnit=10, elecUnit=20
        jdbcTemplate.update("""
            INSERT INTO invoice (
                invoice_status, net_amount, penalty_total, sub_total,
                contract_id, create_date, due_date,
                requested_water_unit, requested_electricity_unit
            )
            VALUES (
                1, 9000, 0, 9000,
                1,
                TO_TIMESTAMP(? || '-10 10:00:00', 'YYYY-MM-DD HH24:MI:SS'),
                TO_TIMESTAMP(? || '-25 23:59:59', 'YYYY-MM-DD HH24:MI:SS'),
                10, 20
            )
            """, lastMonthBase, lastMonthBase);

        // เรียก service
        Map<String, Object> usage = dashboardService.getRoomUsage();

        // ต้องมี room 101 อยู่ใน map
        assertThat(usage).containsKey("101");

        @SuppressWarnings("unchecked")
        Map<String, Object> roomUsage = (Map<String, Object>) usage.get("101");

        @SuppressWarnings("unchecked")
        List<String> categories = (List<String>) roomUsage.get("categories");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> series = (List<Map<String, Object>>) roomUsage.get("series");

        assertThat(categories).hasSize(6);
        assertThat(series).hasSize(2); // Water + Electricity

        String lastMonthLabel = lastMonth.format(MONTH_FORMATTER);
        int idx = categories.indexOf(lastMonthLabel);
        assertThat(idx).isGreaterThanOrEqualTo(0);

        // หา series ของ water กับ electricity
        Map<String, Object> waterSeries = series.stream()
                .filter(s -> "Water (m³)".equals(s.get("name")))
                .findFirst()
                .orElseThrow();

        Map<String, Object> elecSeries = series.stream()
                .filter(s -> "Electricity (kWh)".equals(s.get("name")))
                .findFirst()
                .orElseThrow();

        @SuppressWarnings("unchecked")
        List<Integer> waterData = (List<Integer>) waterSeries.get("data");
        @SuppressWarnings("unchecked")
        List<Integer> elecData = (List<Integer>) elecSeries.get("data");

        assertThat(waterData).hasSize(6);
        assertThat(elecData).hasSize(6);

        // เดือนที่เรามี invoice → unit ต้องตรง 10 และ 20
        assertThat(waterData.get(idx)).isEqualTo(10);
        assertThat(elecData.get(idx)).isEqualTo(20);
    }

    // -------------------------------------------------------------
    // 5) exportMonthlyUsageCsv()
    // -------------------------------------------------------------
    @Test
    void exportMonthlyUsageCsv_shouldReturnRowsForEachRoomWithGrandTotal() {
        // สร้าง room + tenant + contract_type + package_plan + contract + invoice
        jdbcTemplate.update("INSERT INTO room (room_floor, room_size, room_number) VALUES (1, 30, '101')");
        jdbcTemplate.update("""
            INSERT INTO tenant (national_id, first_name, last_name, phone_number, email)
            VALUES ('4444444444444', 'Charlie', 'Tenant', '003', 'charlie@example.com')
            """);
        jdbcTemplate.update("INSERT INTO contract_type (duration, contract_name) VALUES (12, 'CSVPackage')");
        jdbcTemplate.update("""
            INSERT INTO package_plan (is_active, price, room_size, contract_type_id)
            VALUES (1, 6000, 30, 1)
            """);

        // เลือกเดือน target (เช่น เดือนที่แล้ว)
        YearMonth targetMonth = YearMonth.now().minusMonths(1);
        String yearMonthNormalized = targetMonth.toString(); // "YYYY-MM"
        String inputLabel = targetMonth.format(MONTH_FORMATTER); // "MMM yyyy" ใส่เข้า service

        // contract active ครอบคลุม targetMonth
        jdbcTemplate.update("""
            INSERT INTO contract (
                deposit, rent_amount_snapshot, status,
                package_id, room_id, tenant_id,
                sign_date, start_date, end_date
            )
            VALUES (
                12000, 6000, 1,
                1, 1, 1,
                NOW(), TO_TIMESTAMP(? || '-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
                TO_TIMESTAMP(? || '-28 23:59:59', 'YYYY-MM-DD HH24:MI:SS')
            )
            """, yearMonthNormalized, yearMonthNormalized);

        // ค่าเงินสำหรับใบ invoice:
        int rent = 6000;
        int waterUnit = 5;
        int waterPrice = 20;
        int waterAmount = waterUnit * waterPrice; // 100
        int elecUnit = 10;
        int elecPrice = 7;
        int elecAmount = elecUnit * elecPrice;   // 70
        int totalAmount = rent + waterAmount + elecAmount; // 6170

        // ใบ invoice ใน targetMonth
        jdbcTemplate.update("""
            INSERT INTO invoice (
                invoice_status, net_amount, penalty_total, sub_total,
                contract_id, create_date, due_date,
                requested_rent,
                requested_water_unit, requested_water,
                requested_electricity_unit, requested_electricity
            )
            VALUES (
                1, ?, 0, ?,
                1,
                TO_TIMESTAMP(? || '-10 10:00:00', 'YYYY-MM-DD HH24:MI:SS'),
                TO_TIMESTAMP(? || '-25 23:59:59', 'YYYY-MM-DD HH24:MI:SS'),
                ?,
                ?, ?,
                ?, ?
            )
            """,
                totalAmount, totalAmount,
                yearMonthNormalized, yearMonthNormalized,
                rent,
                waterUnit, waterPrice,
                elecUnit, elecPrice
        );

        // เรียก service
        List<String[]> csv = dashboardService.exportMonthlyUsageCsv(inputLabel);

        // รูปแบบ: header + 1 row (room101) + grand total = 3 แถว
        assertThat(csv).hasSize(3);

        String[] header = csv.get(0);
        assertThat(header).containsExactly(
                "Room", "Tenant", "Package",
                "Rent (฿)", "Water Unit", "Water (฿)",
                "Electric Unit", "Electricity (฿)", "Total Amount (฿)"
        );

        String[] row = csv.get(1);
        assertThat(row[0]).isEqualTo("101");                    // Room
        assertThat(row[1]).isEqualTo("Charlie Tenant");         // Tenant name
        assertThat(row[2]).isEqualTo("CSVPackage");             // ContractType.contract_name
        assertThat(row[3]).isEqualTo(String.valueOf(rent));     // Rent
        assertThat(row[4]).isEqualTo(String.valueOf(waterUnit));
        assertThat(row[5]).isEqualTo(String.valueOf(waterAmount));
        assertThat(row[6]).isEqualTo(String.valueOf(elecUnit));
        assertThat(row[7]).isEqualTo(String.valueOf(elecAmount));
        assertThat(row[8]).isEqualTo(String.valueOf(totalAmount));

        String[] grandTotalRow = csv.get(2);
        assertThat(grandTotalRow[7]).isEqualTo("Grand Total");
        assertThat(grandTotalRow[8]).isEqualTo(String.valueOf(totalAmount));
    }
}
