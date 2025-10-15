package com.organicnow.backend.repository;

import com.organicnow.backend.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    // ✅ ของคุณ: ดึง invoice ตาม contract (เรียงจากใหม่ไปเก่า)
    List<Invoice> findByContact_IdOrderByIdDesc(Long contractId);

    // ✅ ของเพื่อน: ดึง invoice ตาม contract
    List<Invoice> findByContact_Id(Long contractId);
    
    /**
     * ✅ Dashboard: สรุปการเงินย้อนหลัง 12 เดือน
     *   - onTime  = จ่ายตรงเวลา (invoice_status = 1 และ penalty_total = 0)
     *   - penalty = จ่ายแต่มีค่าปรับ (invoice_status = 1 และ penalty_total > 0)
     *   - overdue = ค้างจ่าย (invoice_status = 0)
     */
    @Query(value = """
        SELECT to_char(i.create_date, 'YYYY-MM') AS month,
               SUM(CASE WHEN i.invoice_status = 1 AND (i.penalty_total IS NULL OR i.penalty_total = 0) THEN 1 ELSE 0 END) AS onTime,
               SUM(CASE WHEN i.invoice_status = 1 AND i.penalty_total > 0 THEN 1 ELSE 0 END) AS penalty,
               SUM(CASE WHEN i.invoice_status = 0 THEN 1 ELSE 0 END) AS overdue
        FROM invoice i
        WHERE i.create_date >= date_trunc('month', CURRENT_DATE) - INTERVAL '11 months'
        GROUP BY to_char(i.create_date, 'YYYY-MM')
        ORDER BY month
    """, nativeQuery = true)
    List<Object[]> countFinanceLast12Months();

    /**
     * ✅ ดึงข้อมูล Invoice พร้อม Tenant ที่ถูกต้อง (ตาม room และ contract)
     */
    @Query(value = """
        SELECT 
            i.invoice_id, i.create_date, i.due_date, i.invoice_status, 
            i.pay_date, i.pay_method, i.sub_total, i.penalty_total, i.net_amount,
            t.first_name, t.last_name, t.national_id, t.phone_number, t.email,
            r.room_floor, r.room_number,
            ct.contract_name, pp.price,
            c.sign_date, c.start_date, c.end_date
        FROM invoice i
        INNER JOIN contract c ON i.contract_id = c.contract_id
        INNER JOIN room r ON c.room_id = r.room_id
        INNER JOIN tenant t ON c.tenant_id = t.tenant_id
        INNER JOIN package_plan pp ON c.package_id = pp.package_id
        INNER JOIN contract_type ct ON pp.contract_type_id = ct.contract_type_id
        WHERE c.status = 1
        ORDER BY i.invoice_id DESC
    """, nativeQuery = true)
    List<Object[]> findAllInvoicesWithTenantDetails();
}